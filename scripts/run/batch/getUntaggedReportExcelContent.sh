#!/bin/bash
cd $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd );

echo ""
echo ""
echo ""
echo ""

mongo localhost:27017/taxonomy --eval 'cursor=db.iaViews.aggregate([{$match:{categories:{$size:0}}},{$group:{_id:"$series",nbOfDocs:{$sum:1}}},{$sort:{_id:1}}]);

var separator=";";
print("Series;Nb of documents");
while(cursor.hasNext()){
	doc=cursor.next(); 
	print(doc._id + separator + doc.nbOfDocs)
};'
