# Distributed-Weather-Stations-Monitoring
This was AlexU CSE 493: Designing Data Intensive Applications course project. 
\
\
Distributed Weather Stations Monitoring is a distributed system that fetches weather information from multiple sources, archives and visualizes it.

## Features
- Reading from multiple stations using kafka streams.
- Archiving the latest values from each station efficiently using Riak Bitcask.
- Archiving and partitioning the data depending on the weather stations in Parquet files.
- Indexing and Visualizing the archived data using Elastic search and Kibana.

## Deployment (WIP)
The system is fully deployed using Docker and K8s (Kubernetes) but it might need a decent node/machine to be capable of deploying all the services.


