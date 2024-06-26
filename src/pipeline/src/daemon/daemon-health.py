from flask import Flask

app = Flask(__name__)

@app.route('/healthcheck')
def healthcheck():
    return 'Healthy', 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=4001)
