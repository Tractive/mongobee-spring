package com.tractive.mongobee

import com.github.mongobee.dao.ChangeEntryDao
import com.github.mongobee.exception.MongobeeChangeSetException
import com.github.mongobee.exception.MongobeeConfigurationException
import com.github.mongobee.exception.MongobeeConnectionException
import com.github.mongobee.exception.MongobeeException
import com.github.mongobee.utils.ChangeService
import com.mongodb.MongoClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.env.Environment
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class MongobeeSpring(private val environment: Environment,
                     val mongoClient: MongoClient,
                     private val dao: ChangeEntryDao,
                     private val mongbeeProperties: MongobeeProperties,
                     mongoProperties: MongoProperties) : ApplicationContextAware, InitializingBean {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MongobeeSpring::class.java)
    }

    private val database = mongbeeProperties.database ?: mongoProperties.database
    private lateinit var applicationContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    override fun afterPropertiesSet() = execute()

    @Throws(MongobeeException::class)
    fun execute() {
        validateConfig()

        dao.connectMongoDb(this.mongoClient, database)

        if (!dao.acquireProcessLock()) {
            LOGGER.error("Mongobee did not acquire process lock. Exiting.")
            return
        }

        LOGGER.info("Mongobee acquired process lock, starting the data migration sequence..")

        try {
            executeMigration()
        } finally {
            LOGGER.info("Mongobee is releasing process lock.")
            dao.releaseProcessLock()
        }

        LOGGER.info("Mongobee has finished his job.")
    }

    @Throws(MongobeeConnectionException::class, MongobeeException::class)
    private fun executeMigration() {
        val service = ChangeService(mongbeeProperties.changeLogsScanPackage, environment)

        val changelogs = service.fetchChangeLogs()

        if (changelogs.isEmpty()) {
            LOGGER.info("No change logs found in scan package ${mongbeeProperties.changeLogsScanPackage}")
            return
        }

        for (changelogClass in changelogs) {
            try {
                val changelogInstance = changelogClass.getConstructor().newInstance()
                val changesetMethods = service.fetchChangeSets(changelogInstance!!.javaClass)

                for (changesetMethod in changesetMethods) {
                    val changeEntry = service.createChangeEntry(changesetMethod)

                    try {
                        when {
                            dao.isNewChange(changeEntry) -> {
                                executeChangeSetMethod(changesetMethod, changelogInstance)
                                dao.save(changeEntry)
                                LOGGER.info("$changeEntry applied")
                            }
                            service.isRunAlwaysChangeSet(changesetMethod) -> {
                                executeChangeSetMethod(changesetMethod, changelogInstance)
                                LOGGER.info("$changeEntry reapplied")
                            }
                            else -> LOGGER.info("$changeEntry passed over")
                        }
                    } catch (e: MongobeeChangeSetException) {
                        LOGGER.error("Failed to execute changeset ${changeEntry.changeId}: ${e.message}", e)
                        throw e
                    }
                }
            } catch (e: NoSuchMethodException) {
                throw MongobeeException("Failed to execute changelog: ${e.message}", e)
            } catch (e: IllegalAccessException) {
                throw MongobeeException("Failed to execute changelog: ${e.message}", e)
            } catch (e: InvocationTargetException) {
                val targetException = e.targetException
                throw MongobeeException("Failed to execute changelog: ${targetException.message}", e)
            } catch (e: InstantiationException) {
                throw MongobeeException("Failed to execute changelog: ${e.message}", e)
            }
        }
    }

    @Throws(IllegalAccessException::class, InvocationTargetException::class, MongobeeChangeSetException::class)
    private fun executeChangeSetMethod(changeSetMethod: Method, changeLogInstance: Any) {
        try {
            return changeSetMethod.parameterTypes
                .map {
                    applicationContext.getBean(it)
                }
                .run {
                    changeSetMethod.invoke(changeLogInstance, *this.toTypedArray())
                }
        } catch (e: Throwable) {
            LOGGER.error("Exception during change set execution: ${e.message}", e)
            throw MongobeeChangeSetException("Exception during change set execution")
        }
    }

    @Throws(MongobeeConfigurationException::class)
    private fun validateConfig() {
        if (database.isNullOrEmpty()) {
            throw MongobeeConfigurationException("Database is not set, please specify mongobee.database or spring.data.mongodb.database property")
        }
    }
}
