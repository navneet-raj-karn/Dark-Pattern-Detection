import requests
from pprint import pprint

def aichecker(mytext):
    response = requests.post(
        "https://api.sapling.ai/api/v1/aidetect",
        json={"key": "15NEGFKAOVVIOMUV411EDSJQ2ZGE2S55", "text": mytext},
    )
    response_json = response.json()  # Parse JSON response
    
    # Print entire JSON response for debugging
    print("API Response:", response_json)

    # Check if 'score' key exists in the response dictionary
    if 'score' in response_json:
        score = response_json['score']
        return round(score * 100, 2)
    else:
        # Handle the case where 'score' key is not present
        print("Error: 'score' key not found in the response")
        return None

