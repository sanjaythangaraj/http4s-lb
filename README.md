# A Functional Load Balancer with http4s

Following the tutorial from [Rock the JVM](https://rockthejvm.com/articles/a-functional-load-balancer-with-scala-http4s-and-cats-effect)

## Requirements

1. sbt
2. scala 3.3.5
3. JDK 23
4. python

## Run tests

```bash
sbt test
```

## Run the python backends

```bash
python backend.py <port>
```

launch:

`python backend.py 8081`
`python backend.py 8082`
`python backend.py 8083`

## Run the loadbalancer

```bash
scala lb.jar
```

```
20:56:36.617 [io-compute-1] INFO com.example.loadbalancer.Main -- Starting server on localhost:8080
20:56:37.813 [io-compute-2] INFO org.http4s.ember.server.EmberServerBuilderCompanionPlatform -- Ember-Server service bound to address: 127.0.0.1:8080
20:56:37.908 [io-compute-2] INFO com.example.loadbalancer.services.SendAndExpect -- [HEALTH-CHECK] checking http://localhost:8081 health
20:56:38.908 [io-compute-7] INFO com.example.loadbalancer.services.SendAndExpect -- http://localhost:8081 is alive
20:56:41.928 [io-compute-7] INFO com.example.loadbalancer.services.SendAndExpect -- [HEALTH-CHECK] checking http://localhost:8082 health
20:56:41.960 [io-compute-7] INFO com.example.loadbalancer.services.SendAndExpect -- http://localhost:8082 is alive
20:56:44.979 [io-compute-5] INFO com.example.loadbalancer.services.SendAndExpect -- [HEALTH-CHECK] checking http://localhost:8083 health
20:56:44.995 [io-compute-2] INFO com.example.loadbalancer.services.SendAndExpect -- http://localhost:8083 is alive
20:56:48.009 [io-compute-2] INFO com.example.loadbalancer.services.SendAndExpect -- [HEALTH-CHECK] checking http://localhost:8081 health
20:56:48.017 [io-compute-7] INFO com.example.loadbalancer.services.SendAndExpect -- http://localhost:8081 is alive
20:56:51.023 [io-compute-7] INFO com.example.loadbalancer.services.SendAndExpect -- [HEALTH-CHECK] checking http://localhost:8082 health
20:56:51.039 [io-compute-2] INFO com.example.loadbalancer.services.SendAndExpect -- http://localhost:8082 is alive
20:56:54.055 [io-compute-3] INFO com.example.loadbalancer.services.SendAndExpect -- [HEALTH-CHECK] checking http://localhost:8083 health
20:56:54.055 [io-compute-7] INFO com.example.loadbalancer.services.SendAndExpect -- http://localhost:8083 is alive
20:56:57.077 [io-compute-7] INFO com.example.loadbalancer.services.SendAndExpect -- [HEALTH-CHECK] checking http://localhost:8081 health
20:56:57.079 [io-compute-6] INFO com.example.loadbalancer.services.SendAndExpect -- http://localhost:8081 is alive
```

## Hit the loadbalancer

```bash
> curl localhost:8080
hello from http://localhost:8081
> curl localhost:8080
hello from http://localhost:8082
> curl localhost:8080
hello from http://localhost:8083
> curl localhost:8080
hello from http://localhost:8081
> curl localhost:8080
hello from http://localhost:8082
> curl localhost:8080
hello from http://localhost:8083
> curl localhost:8080
hello from http://localhost:8081
```

## Kill a backend

```
21:06:02.842 [io-compute-3] WARN com.example.loadbalancer.services.SendAndExpect -- http://localhost:8081 is dead
21:06:05.846 [io-compute-3] INFO com.example.loadbalancer.services.SendAndExpect -- [HEALTH-CHECK] checking http://localhost:8082 health
21:06:05.849 [io-compute-2] INFO com.example.loadbalancer.services.SendAndExpect -- http://localhost:8082 is alive
21:06:08.860 [io-compute-2] INFO com.example.loadbalancer.services.SendAndExpect -- [HEALTH-CHECK] checking http://localhost:8083 health
21:06:08.876 [io-compute-6] INFO com.example.loadbalancer.services.SendAndExpect -- http://localhost:8083 is alive
21:06:11.889 [io-compute-3] INFO com.example.loadbalancer.services.SendAndExpect -- [HEALTH-CHECK] checking http://localhost:8081 health
21:06:13.928 [io-compute-2] WARN com.example.loadbalancer.services.SendAndExpect -- http://localhost:8081 is dead
21:06:16.934 [io-compute-2] INFO com.example.loadbalancer.services.SendAndExpect -- [HEALTH-CHECK] checking http://localhost:8082 health
21:06:16.950 [io-compute-7] INFO com.example.loadbalancer.services.SendAndExpect -- http://localhost:8082 is alive
21:06:19.960 [io-compute-1] INFO com.example.loadbalancer.services.SendAndExpect -- [HEALTH-CHECK] checking http://localhost:8083 health
21:06:19.960 [io-compute-0] INFO com.example.loadbalancer.services.SendAndExpect -- http://localhost:8083 is alive
21:06:22.972 [io-compute-0] INFO com.example.loadbalancer.services.SendAndExpect -- [HEALTH-CHECK] checking http://localhost:8081 health
```

`localhost:8081` is dead.

## Hit the loadbalancer again

```
> curl localhost:8080
hello from http://localhost:8082
> curl localhost:8080
hello from http://localhost:8083
> curl localhost:8080
hello from http://localhost:8082
> curl localhost:8080
hello from http://localhost:8083
> curl localhost:8080
hello from http://localhost:8082
```

## Restart the stopped backend

```
21:12:08.565 [io-compute-0] WARN com.example.loadbalancer.services.SendAndExpect -- http://localhost:8081 is dead
21:12:11.571 [io-compute-0] INFO com.example.loadbalancer.services.SendAndExpect -- [HEALTH-CHECK] checking http://localhost:8082 health
21:12:11.571 [io-compute-4] INFO com.example.loadbalancer.services.SendAndExpect -- http://localhost:8082 is alive
21:12:14.579 [io-compute-0] INFO com.example.loadbalancer.services.SendAndExpect -- [HEALTH-CHECK] checking http://localhost:8083 health
21:12:14.579 [io-compute-2] INFO com.example.loadbalancer.services.SendAndExpect -- http://localhost:8083 is alive
21:12:17.587 [io-compute-2] INFO com.example.loadbalancer.services.SendAndExpect -- [HEALTH-CHECK] checking http://localhost:8081 health
21:12:19.774 [io-compute-6] INFO com.example.loadbalancer.services.SendAndExpect -- http://localhost:8081 is alive
21:12:22.794 [io-compute-6] INFO com.example.loadbalancer.services.SendAndExpect -- [HEALTH-CHECK] checking http://localhost:8082 health
21:12:22.794 [io-compute-1] INFO com.example.loadbalancer.services.SendAndExpect -- http://localhost:8082 is alive
21:12:25.808 [io-compute-1] INFO com.example.loadbalancer.services.SendAndExpect -- [HEALTH-CHECK] checking http://localhost:8083 health
21:12:25.808 [io-compute-1] INFO com.example.loadbalancer.services.SendAndExpect -- http://localhost:8083 is alive
```

## Hit the loadbalancer again

```bash
> curl localhost:8080
hello from http://localhost:8083
> curl localhost:8080
hello from http://localhost:8082
> curl localhost:8080
hello from http://localhost:8081
> curl localhost:8080
hello from http://localhost:8083
> curl localhost:8080
hello from http://localhost:8082
> curl localhost:8080
hello from http://localhost:8081
> curl localhost:8080
hello from http://localhost:8083
```