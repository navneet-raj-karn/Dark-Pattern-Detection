var fileUrl = chrome.runtime.getURL('dataset.txt');
console.log(fileUrl);
fetch(fileUrl)
    .then(response => response.text())
    .then(content => {
        const patterns = content.split('\n').filter(Boolean).map(pattern => pattern.trim());
        var bodyText = document.body.innerHTML;
        var matchCounter = 0;

        for (var i = 0; i < patterns.length; i++) {
            var pattern = patterns[i].trim();

            var patternClass = "";
            var tooltipTitle = "";

            if (pattern.includes("Only left in stock.")) {
                patternClass = "only-left-class";
                tooltipTitle = "Urgency: Places deadlines on things to make them appear more desirable";
            } else if (pattern.includes("in stock") || pattern.includes("claimed")) {
                patternClass = "in-stock-class";
                tooltipTitle = "Scarcity: Tries to increase the value of something by making it appear to be limited in availability";
            } else if (pattern.includes("Ends") && pattern.includes("in") || pattern.includes("Order within") || pattern.includes("Deal of the day") || pattern.includes("Only 3 left")){
                patternClass = "ends-class";
                tooltipTitle = "Special Offer: Highlighting a deal of the day";
            } else if (pattern.includes("bought in past month") || pattern.includes("Frequently bought together")) {
                patternClass = "last-class";
                tooltipTitle = "Social Proof: Gives the perception that a given action or product has been approved by other people";
            } else if (pattern.includes("Bestseller") || pattern.includes("Best Seller") || pattern.includes("Best-selling")) {
                patternClass = "best-class";
                tooltipTitle = "Misdirection: Aims to deceptively incline a user towards one choice over the other";
            } else if (pattern.includes("With Exchange") || pattern.includes("Add a Protection Plan") || pattern.includes("No Cost EMI") || pattern.includes("3 months Prime membership")){
                patternClass = "exchange-class";
                tooltipTitle = "Sneaking: Coerces users to act in ways that they would not normally act by obscuring information";
            } else if (pattern.includes("Manage Cookie Preferences") || pattern.includes("Tracking Technologies") || pattern.includes("Cookie Policy.") || pattern.includes("Reject All")){
                patternClass = "best-class";
                tooltipTitle = "Privacy intrusion, a dark pattern that stealthily strips away personal boundaries and autonomy in the digital realm.";
            }else if (pattern.includes("Free Trial")){
                patternClass = "best-class";
                tooltipTitle = "Subscription trickery in the dark pattern of 'bait-and-switch' manipulates users into unwittingly signing up for recurring payments under false pretenses";
            }
           
            var regex = new RegExp(`(?<!["'])\\b${pattern.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}\\b(?!>)`, 'gi');

            bodyText = bodyText.replace(regex, function (match, offset, input) {
                // Check if the parent element is to be excluded
                var parentElement = input.substring(0, offset).match(/<[^>]+>/g).pop();
                if (parentElement && (parentElement.includes('id="imageBlock_feature_div"') || parentElement.includes('class="a-declarative"'))) {
                    return match; // Skip highlighting
                } else {
                    matchCounter++;
                    return `<span class="${patternClass}" title="${tooltipTitle}" data-toggle="tooltip" data-placement="top" data-original-title="${pattern}" style="background-color: yellow;">${match}</span>`;
                }
            });
        }

        sendMatchCounter(matchCounter);

        document.body.innerHTML = bodyText;

        // Enable tooltips using Bootstrap
        $(document).ready(function () {
            $('[data-toggle="tooltip"]').tooltip();
        });
    });

function sendMatchCounter(counter) {
    chrome.runtime.sendMessage({ action: 'sendMatchCounter', matchCounter: counter });
}
