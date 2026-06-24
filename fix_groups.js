db = db.getSiblingDB('mediaserver');
db.devices.updateMany(
  { groupId: "6a33e0998beb5d2dece41178" },
  { $set: { group: "gd-1" } }
);
printjson(db.devices.find({ groupId: "6a33e0998beb5d2dece41178" }, {deviceName: 1, group: 1, groupId: 1}).toArray());
