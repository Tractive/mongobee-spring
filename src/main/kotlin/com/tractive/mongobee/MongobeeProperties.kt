package com.tractive.mongobee

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

@Validated
@ConfigurationProperties("mongobee")
class MongobeeProperties {
    /** The name of the changelog collection name */
    @NotBlank
    var changeLogCollectionName = "dbchangelog"

    /** The name of the lock collection name */
    @NotBlank
    var lockCollectionName = "mongobeelock"

    var waitForLock = false

    var changeLogLockWaitTime = 5L

    var changeLogLockPollRate = 10L

    var throwExceptionIfCannotObtainLock = false

    /** The package which will be scanned for changelogs */
    @NotBlank
    lateinit var changeLogsScanPackage: String

    /** The name of the database used by Mongobee. If not provided spring.data.mongodb.database is used. */
    var database: String? = null

    /** If false MongobeeSpring will not be configured / executed */
    var enabled = true
}
