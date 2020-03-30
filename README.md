# BA_Project

## Definitions
Authorization Server: is an authentication server built up by the automakers separately, which could authenticate if a vehicle could be trusted, if yes, it would generate token back to the vehicle.

Root CA: is a root CA in vehicular PKI, built up by the automakers separately. 

Long-Term CA(LTCA): is a subordinate CA of a root CA, it issues LTC for the vehicles. 

token: is an object encapsulating the security identity of a vehicle. Vehicles can use this token to retrieve a root certificate, or apply a new LTC by a LTCA


## Design Structure
Database with elasticsearch

Notification through RabbitMq

## Start

Build base image
```
docker build -t baproject:latest . to build a general work image

```
Then 
```
docker-compose build

```

To run Tests

```
./continuingTestForDelay.sh

```
or 

```
./continuingTestForPacketLoss.sh
```

 
