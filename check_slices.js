db = db.getSiblingDB('mediaserver');
printjson(db.content.find({_id: {$in: [ObjectId('6a33e1498beb5d2dece41184'), ObjectId('6a33e1498beb5d2dece41187'), ObjectId('6a33e1498beb5d2dece41185')]}}).toArray());
