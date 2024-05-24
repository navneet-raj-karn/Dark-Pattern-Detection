from flask import Flask, request, jsonify
from flask_cors import CORS
import requests
import json
import urllib.parse
import pandas as pd

# Importing custom functions
from function.Scrape import get_reviews, get_tags
from function.reviewCheck import aichecker

import tensorflow as tf

# Load the h5 model
model_path = 'function/model.h5'
# App initiation
app = Flask(__name__)
CORS(app)


import urllib.parse
import requests
import json
from flask import Flask, request, jsonify

class IPQS:
    key = "4DeRfiM6GPCwYdV5SgKMgERTvOrfuDdk"

    def malicious_url_scanner_api(self, url: str, vars: dict = {}) -> dict:
        url = "https://www.ipqualityscore.com/api/json/url/%s/%s" % (
            self.key,
            urllib.parse.quote_plus(url),
        )
        print("API URL:", url)  # Debugging statement
        x = requests.get(url, params=vars)
        print("API Response:", x.text)  # Debugging statement
        return json.loads(x.text)

# ------------------------ Malicious Scanner Function ------------------------ #
app = Flask(__name__)

@app.route("/scanweb", methods=["POST"])
def scan_threat():
    data = request.get_json()
    tabURL = data.get("url", "URL not found")
    ipqs = IPQS()
    result = ipqs.malicious_url_scanner_api(url=tabURL)
    score = result.get("risk_score", 0)

    if score >= 0 and score < 20:
        category = "Safe & Trusted"
    elif score >= 20 and score < 40:
        category = "Low"
    elif score >= 40 and score < 60:
        category = "Moderate"
    elif score >= 60 and score < 80:
        category = "High"
    elif score >= 80 and score < 90:
        category = "Potentially Scam"
    elif score >= 90 and score <= 100:
        category = "Scam"
    else:
        category = "Uncategorized"

    result_message = f"Risk Category: {category}"
    is_scam = category == "Scam"
    
    print(result_message)
    return jsonify({"category": category, "isScam": is_scam, "riskScore": score})


# -------------------------- REVIEW SCANNER FUNCTION ------------------------- #
def scan_review(taburl, attributes, reviews):
    percentList = []
    for i in reviews["Reviews"]:
        fakePercent = aichecker(i)
        percentList.append(fakePercent)

    if len(percentList) != 0:
        finalPercent = sum(percentList) / len(percentList)

        if finalPercent < 20.0:
            msg = "Reviews are genuine"
        elif finalPercent > 20.0:
            msg = "Reviews might be fake"
        elif finalPercent > 50.0:
            msg = "Reviews are fake."
        # result_msg1 = f"Fake Reviews : {round(finalPercent, 2)}%\n{msg}"
        result_msg1 = f"{msg}"

    else:
        result_msg1 = ""
    print(result_msg1)
    return result_msg1


# -------------------------- CSV OPERATION FUNCTION -------------------------- #
def csv_operations(dict1, dict2, csvpath):
    final_dict = {**dict1, **dict2}
    required_keys = ["Description", "Availability", "Price", "Reviews"]
    if (
        all(key in final_dict for key in required_keys)
        and len(final_dict["Reviews"]) != 0
    ):
        df = pd.DataFrame([final_dict])
        try:
            file = pd.read_csv("test.csv")
            final_df = pd.concat([file, df], ignore_index=True)
        except FileNotFoundError:
            final_df = df
        final_df.to_csv(csvpath, index=False)
        result_msg2 = "All attributes found"
    else:
        result_msg2 = "Not all attributes are present"
    print(result_msg2)
    return result_msg2


# ----------------------- FINAL ATTRIBUTE SCAN FUNCTION ---------------------- #
@app.route("/scan", methods=["POST"])
def check_all():
    data = request.get_json()
    taburl = data.get("url", "URL not found")
    attributes = get_tags(taburl)
    reviews = get_reviews(taburl)
    csvpath = "./test.csv"

    reviews_scan = scan_review(taburl, attributes, reviews)
    csv_op = csv_operations(attributes, reviews, csvpath)

    return jsonify({"message1": reviews_scan, "message2": csv_op})


# ------------------------------- MAIN FUNCTION ------------------------------ #
if __name__ == "__main__":
    app.run(debug=True, port=1100)
