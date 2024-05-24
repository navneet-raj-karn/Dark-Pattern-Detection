// Check if the current URL matches any blacklisted URL
chrome.storage.local.get(['blacklist'], function (result) {
    var blacklist = result.blacklist || [];
    var currentUrl = window.location.href;
    if (blacklist.includes(currentUrl)) {
        // Display your popup logic here
        alert("This site is in your blacklist!");
    }
});
