import os
from flask import Flask, request, jsonify
from pymongo import MongoClient
from datetime import datetime
from dotenv import load_dotenv

load_dotenv()

app = Flask(__name__)

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
    reports = list(db.reports.find({}, {'_id': 0}).limit(10).sort("received_at", -1))
    return jsonify(reports), 200

if __name__ == '__main__':
    app.run(debug=True)