db = db.getSiblingDB('mediaserver');
var deviceId = "6a33e5378beb5d2dece41188";
var groupId = "6a33e0998beb5d2dece41178";
var groupName = "gd-1";

db.devices.updateOne(
  { _id: ObjectId(deviceId) },
  { $set: { groupId: groupId, groupName: groupName, group: groupName } }
);

db.groups.updateOne(
  { _id: ObjectId(groupId) },
  { $addToSet: { devices: deviceId } }
);

printjson(db.devices.findOne({ _id: ObjectId(deviceId) }));
printjson(db.groups.findOne({ _id: ObjectId(groupId) }));
