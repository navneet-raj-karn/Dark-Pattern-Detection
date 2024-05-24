# Dark Pattern Detection

## Overview
Welcome to the Dark Pattern Detection repository by Team Innovisionaries. Our project aims to identify and mitigate deceptive design practices in mobile and web browsing to ensure a safer online experience for users. Our solution leverages machine learning to detect various dark patterns and provides additional features to enhance user awareness and protection.

## Repository Structure
- **App**: Contains the mobile application built using Android Kotlin and Android Studio.
- **Extension**: Contains the browser extension code.
- **ML Model**: Contains the machine learning model used for detecting dark patterns.

## Features

### Dark Patterns Detection
1. **Manipulative Language Detection**:
   - Analyzes text content for persuasive and misleading words.
   - Assesses tone, sentiment, and choice of words to flag manipulative language.

2. **User Interface Detection**:
   - Examines web page structures and layouts for misleading designs.
   - Identifies elements intended to confuse or misguide users.

3. **False Urgency Detection**:
   - Recognizes temporal language, countdowns, and urgency-inducing elements.
   - Alerts users about tactics designed to force hasty decisions.

4. **Hidden Costs Detection**:
   - Scans content for indications of additional charges, such as fine print or ambiguous language.
   - Helps users make informed decisions by revealing concealed charges.

5. **Forced Account Creation Detection**:
   - Identifies prompts that coerce users into creating accounts unnecessarily.
   - Prioritizes user autonomy and alerts about forced account creation tactics.

### Additional Features
1. **Transparency Score**:
   - Assesses website safety by analyzing GDPR compliance practices.
   - Assigns a transparency percentage, empowering users to navigate online confidently.

2. **Dark Count**:
   - Provides users with insights into the total number of dark patterns detected.
   - Empowers users with a tangible sense of protection.

3. **Crowd Sourcing**:
   - Allows users to contribute by adding new dark patterns to the repository after validation.
   - Enhances the central repository with community input.

4. **Dark Pattern Identifier**:
   - Highlights and identifies types of dark patterns when users hover over highlighted text.
   - Increases transparency by uncovering manipulative design tactics.

## Process Flow

### Extension Workflow
1. **Text Extraction**: Extracts text from websites.
2. **Backend Processing**: Sends extracted text to the Flask backend for processing.
3. **Preprocessing**: Cleans and tokenizes text, removes stopwords.
4. **ML Model Analysis**: Analyzes text for dark patterns.
5. **Highlighting**: Highlights identified dark pattern sentences on the web page.

### App Workflow
1. **Text Extraction**: Extracts text using the Android WebView component.
2. **TFLite Model Prediction**: Sends text to the TFLite model for prediction.
3. **JavaScript Injection**: Injects code to highlight dark pattern sentences within the WebView.

## Machine Learning Model

### Model Overview
- Uses libraries like Keras, numpy, pandas, and regular expressions.
- Involves extensive preprocessing and data handling.

### Architecture
- Embedding layer, Bidirectional LSTM, GlobalMaxPool1D, BatchNormalization, Dropout, and Dense layers.
- Compiled with RMSprop optimizer and binary cross-entropy loss function.

### Data Handling
- Tokenizes text and initializes embedding matrix with pre-trained GloVe word embeddings.

### Improvements
- Evaluates model accuracy and performance using metrics like the confusion matrix.
- Provides a function for predicting the probability of dark patterns in text.

## Deployment Architecture
- **Current Deployment**: Locally deployed on a machine with a Flask backend.
- **Future Deployment**: Plans to deploy on Heroku with GitHub integration for scalability and automation.
- **Mobile Application**: Developed in Android Kotlin, aiming for deployment on the Google Play Store.
- **Model Optimization**: Converts ML model to a pickle file for easy deployment.

## Getting Started
1. **Clone the Repository**:
    ```sh
    git clone <repository-url>
    cd DarkPatternDetection
    ```

2. **Run the Flask Backend**:
    ```sh
    cd MLModel
    python app.py
    ```

3. **Set up the Browser Extension**:
    - Follow instructions in the `Extension` folder.

4. **Run the Mobile App**:
    - Open the `App` folder in Android Studio and run on an emulator or device.

## Contributing
We welcome contributions from the community. Please read the contribution guidelines and submit your pull requests.

## License
This project is licensed under the MIT License.

## Contact
For any questions or feedback, please contact us at [imnavneet1234@gmail.com].
