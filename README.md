[![](https://jitpack.io/v/tractive/mongobee-spring.svg)](https://jitpack.io/#tractive/mongobee-spring)

# Mongobee Spring

This library provides advanced Spring capabilities to [Mongobee](https://github.com/dalet-oss/mongobee).
It acts as a Spring wrapper around this MongoDB database migration library. 
As no appropriate hooks exist within `com.github.mongobee.Mongobee` a new class `com.tractive.mongobee.MongobeeSpring` was introduced, which internally utilizes the utility classes provided by Mongobee.

## Key features
* ChangeSet methods can be injected with arbitrary Spring beans
* Configurable via spring property files 
* Spring Boot autoconfiguration support

## Integration / Usage
The library is published via [jitpack.jo](https://jitpack.io).
To include it into your project please follow the instructions available [here](https://jitpack.io/#tractive/mongobee-spring).

## Behavior changes
* In case a ChangeSet execution fails the whole migration will be aborted.
