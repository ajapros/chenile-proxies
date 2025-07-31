# chenile-proxies
This contains the source code of all the Proxies modules 

Chenile Proxy Framework provides a way to communicate between services using their interfaces.
There can be two different types of interactions between Chenile Services.
## Messaging and the Observer Pattern
This interaction is useful when a service has multiple subscribers to its events and needs to broadcast to 
them via messaging. 
Consider the example below:
We have FooService that needs to communicate to its subscribers via events. 
FooService - send FooEvent to all Foo observers

public interface FooObserver {
public void invoke(FooEvent event);
}
FooService is not aware of BarService
BarService depends on FooService and is an observer of the FooEvent

public BarService implements FooObserver{
...
}

BarServiceController {
@EventsSubscribedTo("foo")
public void invoke(FooEvent event){

    }
}

FooService talks to Chenile Proxy and gets hold of FooObserverProxy
FooObserverProxy implements FooObserver
FooService says FooObserverProxy.invoke(FooEvent);
If BarService is local then FooObserverProxy makes a local invocation that is synchronous
If BarService is remote then FooObserverProxy sends an event to the foo queue using
ChenilePubSub and any ChenilePublisher that is available (can be Kafka, MQ etc.)



When to use HTTP Proxy?
---------------------------

AService wants to invoke BService
public interface BService {
public void invoke(BPayload payload);
}
AService depends on BService
BService does not depend on AService

public BServiceImpl implements BService{
...
}

BServiceController {

    public void invoke(BPayload payload){
        
    }
}

AService talks to Chenile Proxy and gets hold of BServiceProxy
BServiceProxy implements BService
AService says BService.invoke(BPayload);
If BServiceImpl is local then BServiceProxy makes a local invocation that is synchronous
If BServiceImpl is remote then BServiceProxy uses HTTP to invoke BServiceImpl

# About chenile

Chenile is an open source framework for creating Micro services using Java and Spring Boot. 
Please check the details out at https://chenile.org

It provides an interception framework to decouple functional and non-functional requirements.
Chenile avoids the need to write repetitive code. It encourages modular coding best practices. 

In addition to creating REST services, Chenile services can also be used to create event processors, 
schedulers (with quartz), a file watcher etc. without the need for rewriting the code. 

Chenile has a state machine and an orchestration engine.  

The orchestration engine is internally used by Chenile to provide an interception framework that helps in 
disinter-mediating traffic irrespective of the incoming protocol (HTTP, message etc.)

Hence Chenile also serves like an IN-VM message bus. Chenile also facilitates easy swagger documentation 
(using Spring doc). 
Chenile allows the development of Cucumber based BDD tests with most of the plumbing already in place.
Chenile also is integrated with [keycloak](https://www.keycloak.org/) for security. 

Finally, Chenile ships with its own code generators to ease the development of micro services. 
Please see [Code Generation Repository](https://github.com/rajakolluru/chenile-gen) for more information 
about the code generator.


