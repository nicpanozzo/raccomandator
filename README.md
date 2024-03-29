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
- [ ] uniformare dataset per ML e SC
- [x] modificare SC che prenda in input un utente(cioè basi la raccomandazione su una serie di recensioni e non su un film). Idea: Vettorializzare gli utenti e trovare i piu vicini
- [ ] unire ML e SC ponderando gli score

- [x] filtrare utenti con poche recensioni (pre cosine similarity)

- [x] aggiungere threshold per le co occorrenze (post cosine similarity)
- [x] rifare la seconda matrice cosi che usi la map 

 4373  sbt assembly
 4374  gsutil cp target/scala-2.12/raccomandator-assembly-0.1.2-SNAPSHOT.jar gs://raccomandator
 4375  gcloud dataproc jobs submit spark \\n    --cluster=racclusingle \\n    --class=UserSimilarities1 \\n    --jars=gs://raccomandator/raccomandator-assembly-0.1.2-SNAPSHOT.jar \\n    --region=us-central1 \\n    -- 6666



gcloud dataproc jobs submit spark \    --cluster=racclusingle \                                    
    --class=UserSimilarities1 \
    --jars=gs://raccomandator/raccomandator-assembly-0.1.2-SNAPSHOT.jar \
    --region=us-central1 \
    -- 6666


  gcloud dataproc clusters create ${CLUSTER} --project=${PROJECT} --region=${REGION} --single-node --enable-component-gateway --metric-sources=SPARK_HISTORY_SERVER