package com.tractive.mongobee

import com.github.mongobee.dao.ChangeEntryDao
import com.mongodb.MongoClient
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
@AutoConfigureAfter(MongoAutoConfiguration::class)
@ConditionalOnMissingBean(MongobeeSpring::class)
@ConditionalOnClass(ChangeEntryDao::class, MongoClient::class)
@EnableConfigurationProperties(MongobeeProperties::class)
@ConditionalOnProperty("mongobee.enabled", havingValue = "true", matchIfMissing = true)
class MongobeeAutoConfiguration {

    @Bean
    fun mongobeeSpring(
        environment: Environment,
        mongoClient: MongoClient,
        changeEntryDao: ChangeEntryDao,
        mongbeeProperties: MongobeeProperties,
        mongoProperties: MongoProperties
    ) = MongobeeSpring(
        environment,
        mongoClient,
        changeEntryDao,
        mongbeeProperties,
        mongoProperties
    )

    @Bean
    @ConditionalOnMissingBean
    fun mongobeeChangeEntryDao(mongbeeProperties: MongobeeProperties) =
        ChangeEntryDao(
            mongbeeProperties.changeLogCollectionName,
            mongbeeProperties.lockCollectionName,
            mongbeeProperties.waitForLock,
            mongbeeProperties.changeLogLockWaitTime,
            mongbeeProperties.changeLogLockPollRate,
            mongbeeProperties.throwExceptionIfCannotObtainLock
        )

    @Bean
    @ConditionalOnMissingBean
    fun mongobeeMongoDatabase(changeEntryDao: ChangeEntryDao) = changeEntryDao.mongoDatabase
}
