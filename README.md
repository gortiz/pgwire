# pgWire Java

This project is a Java library that can be used to create servers that speak the Postgres wire protocol, also known as
pgwire.


In [SampleServer.java](src/test/java/pgwire/SampleServer.java) you can see an example on how to implement a very simple
server that always returns the same data.

## TODO

The project is in a very early access and there are things considered basic that are still not implemented, including:

- [ ] Helpers to deal with authentication.
- [ ] An abstract server that implements the cancellation mechanism.
- [ ] More realistic Type libraries.
- [ ] An example that delegates the backend responses on any JDBC connection.
- [ ] An example or abstract class that can be used to work with Strings and byte[] instead of ByteBuf.