# Mimicking Twitter Using an Akka-Based Event-Driven Architecture

_11 Apr 2011_

It is interesting to experiment with CQRS and EDA, which together can provide the underpinnings of some darn scalable Web applications. Twitter is in many ways the canonical scalable Web application. Its interaction consists of mainly reads by the user, with occasional updates resulting in view-level changes for many other users.

An architecture built on Command Query Responsibility Separation has a data flow which can be modeled as a triangle:

![CQRS data flow](https://raw.github.com/JamesEarlDouglas/zapper/master/readme/cqrs-data-flow.png)

The majority of user interaction with the Web application is to pull a representation of the view model via queries. Changes to the system are issued by the user as commands, which are processed by the domain and result in events broadcast to the view.

In this example, the domain and view are each implemented as Akka actors.

The domain actor, `ZapManager`, handles incoming commands from the user (constructed by a thin Scalatra layer) and updates the domain model accordingly. The domain model is a couple of maps which store messages broadcast by each user as well as lists of followers of each user.

The view actor, `ZapViewer`, handles incoming events from the domain as well as incoming queries from the user. Domain events result in one or more updates to the view model, and queries return a subset of the view model. The view model directly maps to what the user will see, so that queries are analogous to SQL selects without the need for joins or other expensive processing. Since domain events drive the only changes to the view, this actor can be replicated as needed to supply user demand.

The result is Zapper, a somewhat Twitter-like Web application that is (theoretically) quite scalable. Zapper supports `@mentions` and following of other users. To see what it looks like, imagine two Zapper users: jmcdoe and kadigan.

![Zapper screenshot for jmcdoe](https://raw.github.com/JamesEarlDouglas/zapper/master/readme/zapper-jmcdoe.png)

![Zapper screenshot for kadigan](https://raw.github.com/JamesEarlDouglas/zapper/master/readme/zapper-kadigan.png)

I didn't implement Event Sourcing, opting to only keep a single current view model. Though this limits the ability to audit or roll-back the state of the system, it yields a simpler architecture. 
