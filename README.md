# BA_Project

## Definitions
Authorization Server: is an authentication server built up by the automakers separately, which could authenticate if a vehicle could be trusted, if yes, it would generate token back to the vehicle.

Root CA: is a root CA in vehicular PKI, built up by the automakers separately. 

Long-Term CA(LTCA): is a subordinate CA of a root CA, it issues LTC for the vehicles. 

token: is an object encapsulating the security identity of a vehicle. Vehicles can use this token to retrieve a root certificate, or apply a new LTC by a LTCA



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
validateToken(partly finished)
validateSignature(needed)
```



 
