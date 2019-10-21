# BA_Project

## Definitions
identity federation: is a federation of multiple automakers, which allows single-sign-on authentication for all vehicles they fabricated.  

V2V connection: is a depluex connection between/among vehicles, allows both vehicle to send/receive data at the same time

Server: is an authentication server built up by the automakers seprately, which could authenticate if a vehicle could be trusted, if yes, it would generate token back to the vehicle.

token: is an object encapsulating the security identity of a vehicle or a service. Vehicles in the same identity federation can use it to require/save data in services or builds V2V connnection between themselves. Services in the same identity federation can use it to require data from other services



## Status
###Vehicle Mock
needed means not yet iomplemented.
mock means the input and output of this function/api has already been definied. 
finished means the function has been totally implemented.

```
Rest-Apis:
manipulation(finished)
buildConnection(needed)
receive(needed)

Functions: 
generateSTK(mock)
requestToken(finished)
encryptData(finished)
buildConnection(needed)
validateToken(finshed)
validateSignature(finished)
send(needed)
receive(needed)
```

### Backend Mock

```
Rest-Apis:
requestToken(finished)
getPublicKey(needed)

Functions: 
generateToken(finished)
validateToken(finished)
sendPublicKey(needed)
validateSignature(finished)
```

### Service Mock
```
Rest-Apis:
saveData(needed)
getData(needed)

Functions: 
generateToken(partly finished)
validateToken(partly finished)
validateSignature(needed)
```



 
