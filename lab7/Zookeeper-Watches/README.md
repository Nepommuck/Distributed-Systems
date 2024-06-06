## How to run

### Zookeeper server
In zookeeper directory:
```
./bin/zkServer.sh start-foreground
```

### Kotlin application
Run `agh.distributedsystems.zookeeper.MainKt.main`

### Zookeeper client

In zookeeper directory:
```
./bin/zkCli.sh -server 127.0.0.1:2181
```


## How to interact
Use zookeeper client to execute commands

### List current znodes
```
ls -R /
```

### Create a watched znode
```
create /a
```

### Create child
To create a child named `child-name`:
```
create /a/child-name
```

### Delete descendant
```
delete /a/some-child/some-descendant
```

### Delete watched znode with all descendants
```
deleteall /a
```
