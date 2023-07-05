# t12sRotationRunOnce

execute a rotation only once for a given token and saves the result.
If you run this step multiple times with the same parameter, the result will be always the same.

Parameter is a map of String to String, example:
```groovy
final def parameter = [
    'teamId': 'your-team-id',
    'teamSecret': 'your team secret',
    'rotationId': 'your-rotation-id_and-not-rotation-name',
    'token': 'release-23.51',
    'verbose': 'true|false' // the key 'verbose' is optional
]
```

Return type is a map with the following structure
```json
{
  "createdAt": "2023-03-18T11:50:01.248496Z",
  "deleteAt": "2023-05-17T11:50:01.248496Z",
  "id": "ROTATION_CONFIG#6492503e-e872-4d34-aca1-72b63b1db528#RUN_RESULT#2023-03-18T11:50:01.248496Z#adam",
  "memberOrder": [
    "adam",
    "peter",
    "alex",
    "lisa",
    "mayer"
  ],
  "name": "Daily Presenter run at 2023-03-18T11:50:01.248496Z Winner adam",
  "rotationId": "6492503e-e872-4d34-aca1-72b63b1db528",
  "settings": {
    "RotationRunner": "RandomRotationRunner"
  },
  "teamId": "t12s-core-t1"
}
```