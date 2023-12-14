To create and execute the jar do the following:
```
sbt assembly
java -jar target/scala-2.13/raccomandator_2.13-0.1.0-SNAPSHOT.jar
OR
spark-submit target/scala-2.13/raccomandator-assembly-0.1.1-SNAPSHOT.jar  
```

To create a cluster on dataproc:
```
gcloud dataproc clusters import my-new-cluster --source cluster.yaml
```

il seguente comando elenca i contenuti del bucket:
```
gsutil ls gs://my-most-unique-bucket-name
```
copia il jar nel bucket:
```
gsutil cp HelloWorld.jar gs://<bucket-name>/
```

per eliminare il cluster:
```
gcloud dataproc clusters delete cluster-name --region=REGION
```
Per eliminare il file jar di Cloud Storage:
```
gsutil rm gs://bucket-name/HelloWorld.jar
```

Puoi eliminare un bucket e tutte le relative cartelle e file con il seguente comando:

```
gsutil rm -r gs://bucket-name/
```
per creare un bucket:
```
gcloud storage buckets create gs://BUCKET_NAME
```


To submit a Spark job that runs the main class of a jar, run:

gcloud dataproc jobs submit spark --cluster=my-cluster --region=us-central1 --jar=my_jar.jar -- arg1 arg2

gcloud dataproc jobs submit spark --cluster=racclu --region=us-central1 --jar=target/scala-2.12/raccomandator-assembly-0.1.1-SNAPSHOT.jar

To submit a Spark job that runs a specific class of a jar, run:


gcloud dataproc jobs submit spark --cluster=my-cluster --region=us-central1 --class=org.my.main.Class --jars=my_jar1.jar,my_jar2.jar -- arg1 arg2
To submit a Spark job that runs a jar that is already on the cluster, run:


gcloud dataproc jobs submit spark --cluster=my-cluster --region=us-central1 --class=org.apache.spark.examples.SparkPi --jars=file:///usr/lib/spark/examples/jars/spark-examples.jar -- 1000




#TODO

creare uno script che crea il cluster e lo elimina
creare uno script che crea il bucket e lo elimina
creare uno script che crea il job e lo elimina
creare uno script che crea il jar e lo elimina
creare uno script che crea il jar e lo copia nel bucket

- [ ] aggiungere il file di configurazione per il cluster
- [ ] aggiungere il file di configurazione per il bucket
- [ ] aggiungere il file di configurazione per il job

configurare ambienti locale, low cost, high performance e dataset differenti   



Steps:
- [ ] creare un jar
  sbt assembly
- [ ] creare un bucket
  gcloud storage buckets create gs://raccomandator
- [ ] copiare il jar nel bucket
  gsutil cp target/scala-2.13/raccomandator-assembly-0.1.1-SNAPSHOT.jar gs://raccomandator
- [ ] copiare i dati nel bucket
  gsutil cp datasets/u.data gs://raccomandator
- [ ] creare un cluster
  gcloud dataproc clusters import racclu --source cluster.yaml --region=us-central1
- [ ] creare un job
  gcloud dataproc jobs submit spark --cluster=racclu --region=us-central1 --jar=target/scala-2.12/raccomandator-assembly-0.1.1-SNAPSHOT.jar
- [ ] eliminare il cluster
  gcloud dataproc clusters delete racclu --region=us-central1
- [ ] eliminare il bucket
  gsutil rm -r gs://raccomandator