// popup.js


// Function to analyze data and flag products
function analyzeData(data) {
  // Extract prices from the data
  const prices = data.map(product => parseFloat(product.price.replace('₹', '').replace(',', '')));

  // Calculate the mean and standard deviation
  const mean = prices.reduce((sum, price) => sum + price, 0) / prices.length;
  const squaredDifferences = prices.map(price => Math.pow(price - mean, 2));
  const variance = squaredDifferences.reduce((sum, squaredDiff) => sum + squaredDiff, 0) / prices.length;
  const standardDeviation = Math.sqrt(variance);

  // Calculate the threshold for suspicious prices (87% deviation)
  //const threshold = mean * 0.87;


  // we want that the price of the product should not deviate more than 3 times the standard deviation 
  // This value can later be changed as well

  // Flag products with prices exceeding the threshold as suspicious
  data.forEach(product => {
    const productPrice = parseFloat(product.price.replace('₹', '').replace(',', ''));
    product.flagged = productPrice < (mean - (3 * standardDeviation));
  });

  // Return the count of flagged products
  return data.filter(product => product.flagged).length;
}


// Function to fetch data from the API
async function fetchData(searchQuery) {
  const apiUrl = 'https://amazon-product-data6.p.rapidapi.com/product-by-text';
  const apiKey = '21e6847fc9msh1bfca0aeb6dcf2ep15b7d3jsn262d5fa3ea2d';

  const options = {
    method: 'GET',
    headers: {
      'X-RapidAPI-Key': apiKey,
      'X-RapidAPI-Host': 'amazon-product-data6.p.rapidapi.com',
    },
    params: {
      keyword: searchQuery,
      page: '1',
      country: 'IN',
    },
  };

  try {
    const response = await fetch(apiUrl, options);
    const data = await response.json();
    return data;
  } catch (error) {
    console.error(error);
    return null;
  }
}

// Function to analyze data and flag products
function analyzeData(data) {
  // Implement your data analysis logic here
  // Example: Check if prices are outliers and set a "flagged" property

  data.forEach(product => {
    // Your data analysis logic here...
  });
}

// Function to update UI based on flagged status
function updateUI(flaggedProductsCount) {
  const countElement = document.getElementById('count');
  countElement.textContent = flaggedProductsCount;

  // You can also update the UI to display a warning if there are flagged products
  const outputElement = document.getElementById('output');
  if (flaggedProductsCount > 0) {
    outputElement.textContent = "Warning: This page might have flagged products.";
  } else {
    outputElement.textContent = ""; // Clear the warning
  }
}

// Function to handle the page load and perform necessary actions
async function onPageLoad() {
  // Get the user's search query from the background script
  chrome.runtime.sendMessage({ action: 'getSearchQuery' }, async (response) => {
    const searchQuery = response.searchQuery;

    if (searchQuery) {
      // Fetch data from the API
      const data = await fetchData(searchQuery);

      if (data) {
        // Analyze data and flag products
        const flaggedProductsCount = analyzeData(data);

        // Update the UI based on the flagged status
        updateUI(flaggedProductsCount);

        // Send a message to the background script with the flagged status
        chrome.runtime.sendMessage({ action: 'updateFlaggedStatus', flaggedProductsCount });
      } else {
        console.log('Failed to fetch data from the API.');
      }
    }
  });
}

// Call onPageLoad when the popup is opened
document.addEventListener('DOMContentLoaded', onPageLoad);
