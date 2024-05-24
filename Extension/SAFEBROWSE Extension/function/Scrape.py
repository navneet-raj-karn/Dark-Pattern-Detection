import requests
from bs4 import BeautifulSoup
import json
import pandas as pd

headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
}


# ------- Find all script tags to find Description,Availibity and Price ------ #
def get_tags(url):
    response = requests.get(url, headers=headers)
    html = response.text
    soup = BeautifulSoup(html, "html.parser")
    script_tags = soup.find_all("script", type="application/ld+json")

    if script_tags:
        script_tag = script_tags[1]

        json_data = json.loads(script_tag.contents[0])

        # Extract information from the JSON data
        description = json_data.get("description", "")
        availability = json_data.get("offers", {}).get("availability", "")
        price = json_data.get("offers", {}).get("price", "")

        product_dict = {
            "Description": description,
            "Availability": availability,
            "Price": price,
        }
        return product_dict
    else:
        print('No script tags with type "application/ld+json" found on the page.')


# ------------------------- Function to scrap reviews ------------------------ #
def get_reviews(url):
    response = requests.get(url, headers=headers)
    html_content = response.text

    start_index = html_content.find('"topReviews":')
    end_index = html_content.find('"topImageReviews"', start_index)
    json_like_string = "{" + html_content[start_index : end_index - 1] + "}"
    json_string = json_like_string
    data = json.loads(json_string)

    review_texts = []
    for review in data["topReviews"]:
        review_texts.append(review["reviewText"])

    review_dict = {"Reviews": review_texts}
    return review_dict
