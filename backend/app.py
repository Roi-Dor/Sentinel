import os
from flask import Flask, request, jsonify
from pymongo import MongoClient
from datetime import datetime
from dotenv import load_dotenv
from bson.objectid import ObjectId
from flask_cors import CORS

load_dotenv()

app = Flask(__name__)
CORS(app)

mongo_uri = os.getenv('MONGO_URI')

# Connect to MongoDB
client = MongoClient(mongo_uri)
db = client.sentinel_db  

@app.route('/api/report', methods=['POST'])
def receive_report():
    data = request.json
    data['received_at'] = datetime.utcnow()
    
    # Save to the 'reports' collection
    db.reports.insert_one(data)
    
    return jsonify({"status": "success", "message": "Report saved"}), 201

@app.route('/api/stats', methods=['GET'])
def get_stats():
    reports_cursor = db.reports.find().limit(10).sort("received_at", -1)
    
    reports = []
    for doc in reports_cursor:
        doc['_id'] = str(doc['_id']) 
        reports.append(doc)

    return jsonify(reports), 200

@app.route('/api/report/<id>', methods=['DELETE'])
def delete_report(id):
    try:
        db.reports.delete_one({'_id': ObjectId(id)})
        return jsonify({"status": "success", "message": "Report deleted"}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)