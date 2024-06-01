# Orthopedic-Department

A **Kotlin** / **gradle** project using [**RabbitMQ**](https://www.rabbitmq.com/) message-broker

## Communication diagram
![Orthopedic-Department diagram](./Orthopedic-Department-diagram.svg)

## How to start

### RabbitMQ
```
docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.13-management
```

### Actors

#### Doctor
Run `edu.agh.distributedsystems.rabbitmq.doctor.DoctorLauncherKt.main` without arguments

#### Technician
Run `edu.agh.distributedsystems.rabbitmq.technician.TechnicianLauncherKt.main` with 2 mandatory arguments:
two different injuries from: `knee`, `hip`, `elbow`. 

#### Administration
Run `edu.agh.distributedsystems.rabbitmq.administration.AdministrationLauncherKt.main` without arguments

## Inspect RabbitMQ
Visit http://localhost:15672/ and log-in with `guest`/`guest` credentials
