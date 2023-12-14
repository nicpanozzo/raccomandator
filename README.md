# Raccomandator

Un sistema di raccomandazioni per film basato su collaborative filtering. Il sistema è stato sviluppato in scala e utilizza Spark per il calcolo distribuito. Il dataset utilizzato è MovieLens 100k.
## STEPS:
- ### creare un jar
```
  sbt assembly
```
per eseguirlo in locale:
  - java -jar target/scala-2.13/raccomandator_2.13-0.1.0-SNAPSHOT.jar
  - spark-submit target/scala-2.13/raccomandator-assembly-0.1.1-SNAPSHOT.jar
- ### creare un cluster e bucket con le config.txt
```
./start_cluster.sh
```
- ### lanciare il job con il jar
```
./submit_job.sh
```
- ### eliminare il cluster
```
./delete_cluster.sh
```

## COMANDI UTILI:
- **creare un jar**
```
sbt assembly
```
- **creare un bucket**
```
gcloud storage buckets create gs://raccomandator
```
- **copiare il jar nel bucket**
```
gsutil cp target/scala-2.13/raccomandator-assembly-0.1.1-SNAPSHOT.jar gs://raccomandator
```
- **copiare i dati nel bucket**
```
gsutil cp datasets/u.data gs://raccomandator
```
- **creare un cluster**
```
gcloud dataproc clusters import racclu --source cluster.yaml --region=us-central1
```
- **creare un job**
```
gcloud dataproc jobs submit spark --cluster=racclu --region=us-central1 --jar=target/scala-2.12/raccomandator-assembly-0.1.1-SNAPSHOT.jar
```
- **eliminare il cluster**
```
gcloud dataproc clusters delete racclu --region=us-central1
```
- **eliminare il bucket**
```
gsutil rm -r gs://raccomandator
```


# TODO
- [ ] configurare ambienti locale, low cost, high performance e dataset differenti  



