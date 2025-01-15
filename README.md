# pgWire Java

This project is a Java library that can be used to create servers that speak the Postgres wire protocol, also known as
pgwire.

Currently this is a toy project and it is not ready to be used in production.
Although this project could be used to create drivers, there are other production ready
postgres drivers. This is why the original focus of this project is to create servers or
proxies that speak the Postgres wire protocol.


## Examples
In [SampleServer.java](src/test/java/pgwire/SampleServer.java) you can see an example on how to implement a very simple
server that always returns the same data.

In [JdbcServer.java](src/test/java/pgwire/JdbcServer.java) you can see an example on how to implement a server that
just plays as a proxy to another JDBC connection.

## How to use

The two main classes that need to be implemented are:
1. [FrontendMessageListener](src/main/java/pgwire/FrontendMessageListener.java),
   which defines the logic that will be executed when a message is received from the client.
2. [PostgresServer](src/main/java/pgwire/PostgresServer.java),
   which defines the port to listen and configures the library to use the previous class.

Whenever a frontend message is received, a method in the FrontendMessageListener will be called. 
This method should apply the corresponding logic and return a response to the client.
In order to do that, it is recommended to use [BackendMessageSender](src/main/java/pgwire/BackendMessageSender.java),
which hides the complexity of sending back messages to the client.

Both FrontendMessageListener and BackendMessageSender have one method per message defined in the pg wire protocol.
The javadoc of each method try to explain what is the expected behavior of each method, but it is recommended to
consult the [protocol page](https://www.postgresql.org/docs/current/protocol.html) in the Postgres documentation and
even read the source code of pglib or https://github.com/sunng87/pgwire

## TODO

The project is in a very early access and there are things considered basic that are still not implemented, including:

- [ ] Helpers to deal with authentication.
- [ ] An abstract server that implements the cancellation mechanism.
- [ ] More realistic Type libraries.
- [x] An example that delegates the backend responses on any JDBC connection.
- [ ] An example or abstract class that can be used to work with Strings and byte[] instead of ByteBuf.