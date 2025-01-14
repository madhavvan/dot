from flask import Flask, request, jsonify
from transformers import pipeline

# Initialize Flask app
app = Flask(__name__)

# Load the Hugging Face model (you can change the model as needed)
generator = pipeline('text-generation', model="EleutherAI/gpt-neo-2.7B")

@app.route('/generate', methods=['POST'])
def generate():
    data = request.json
    prompt = data.get('prompt', '')
    result = generator(prompt, max_length=50)
    return jsonify(result[0]['generated_text'])

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)  # Accessible on local network
