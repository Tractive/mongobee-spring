# Mongobee Spring

This library provides advanced Spring capabilities to [Mongobee](https://github.com/mongobee/mongobee).
It acts as a Spring wrapper around this MongoDB database migration library. 
As no appropriate hooks exist within `com.github.mongobee.Mongobee` a new class `com.tractive.mongobee.MongobeeSpring` was introduced, which internally utilizes the utility classes provided by Mongobee.

## Key features
* ChangeSet methods can be incected with arbitrary Spring beans
* Configurable via spring property files 
* Spring Boot autoconfiguration support

## Integration / Usage
Currently this project is not published to any public artifact repository, so you'll need to build it on your own.

## Behavior changes
* In case a ChangeSet execution fails the whole migration will be aborted.