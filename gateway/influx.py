from influxdb_client import InfluxDBClient, Point
from influxdb_client.client.write_api import SYNCHRONOUS
import datetime

# Configuration
INFLUXDB_URL = "http://localhost:8086"     # Adresse de ton InfluxDB
INFLUXDB_TOKEN = "my-super-secret-token"      # Généré depuis l’interface Web
INFLUXDB_ORG = "my-org"          # Organisation définie dans InfluxDB
INFLUXDB_BUCKET = "my-bucket"             # Nom du bucket où écrire les données

# Connexion
client = InfluxDBClient(
    url=INFLUXDB_URL,
    token=INFLUXDB_TOKEN,
    org=INFLUXDB_ORG
)
write_api = client.write_api(write_options=SYNCHRONOUS)

# Fonction d'écriture
def insert_uart_data(room_id: str, mode: str, values: dict):
    """
    Enregistre une mesure dans InfluxDB avec tags et fields.
    Exemple de valeurs : values = {'T': 23, 'H': 50}
    """
    point = Point("uart_data") \
        .tag("room_id", room_id) \
        .tag("mode", mode)

    for key, val in values.items():
        try:
            point = point.field(key, float(val))  # convertit les champs en float
        except ValueError:
            point = point.field(key, str(val))    # enregistre en tant que string sinon

    point = point.time(datetime.datetime.utcnow())

    write_api.write(bucket=INFLUXDB_BUCKET, org=INFLUXDB_ORG, record=point)
    print(f"[InfluxDB] Wrote data: room_id={room_id}, mode={mode}, values={values}")