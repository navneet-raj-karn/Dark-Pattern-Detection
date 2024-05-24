import json
import requests
import urllib
import pandas as pd


class IPQS:
    key = "GQNWfzROpleQ0AXMwIMZtLTobllWgc3V"

    def malicious_url_scanner_api(self, url: str, vars: dict = {}) -> dict:
        key = "GQNWfzROpleQ0AXMwIMZtLTobllWgc3V"
        url = "https://www.ipqualityscore.com/api/json/url/%s/%s" % (
            key,
            urllib.parse.quote_plus(url),
        )
        x = requests.get(url, params=vars)
        print(x.text)
        return json.loads(x.text)

