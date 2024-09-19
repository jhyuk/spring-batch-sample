Spring batch sample
---------------------------------

### 1. Partition
- 상품의 판매 이력을 파일로 쓰는 Job
- HistoryGeneratePartitionJobConfiguration : Partition을 이용한 Job
- 파라미터 totalCount, targetDate를 입력하여 실행

  - totalCount를 gridSize로 나누어 각각의 Step에서 데이터를 읽고 파일로 쓰도록 구성
  - totalCount = 5000000 (오백만), gridSize = 7 로 설정하고 실행함. 
  - 각 스텝은 5000000 / 7 = 714285개의 데이터를 읽게 되며 714285를 chunkSize만큼 나눠서 처리함.
  

  **실행 결과**

      ```
      Running default command line with: [totalCount=5000000, targetDate=20240911]
      Job: [SimpleJob: [name=historyGeneratorJob]] launched with the following parameters: [{'totalCount':'{value=5000000, type=class java.lang.String, identifying=true}','targetDate':'{value=20240911, type=class java.lang.String, identifying=true}'}]
      Executing step: [stepManager]
      Step: [historyGenerateStep:partition1] executed in 56s312ms
      Step: [historyGenerateStep:partition0] executed in 56s315ms
      Step: [historyGenerateStep:partition6] executed in 56s398ms
      Step: [historyGenerateStep:partition3] executed in 56s398ms
      Step: [historyGenerateStep:partition2] executed in 56s461ms
      Step: [historyGenerateStep:partition5] executed in 56s480ms
      Step: [historyGenerateStep:partition4] executed in 56s545ms
      Step: [stepManager] executed in 56s552ms
      ```
### 멀티쓰레드 스텝과의 차이점
  - 멀티쓰레드는 단일 스텝을 chunk 단위로 쓰레드를 생성하여 병렬로 처리함
  - 멀티쓰레드 스텝에서는 동일한 데이터를 여러 쓰레드가 처리하므로 ItemReader, ItemWriter 등 동시성 문제를 고려해야함.
  - 멀티쓰레드의 모든 쓰레드는 같은 트랜잭션을 공유. (커밋되면 모든 데이터 커밋, 롤백되면 모든 데이터 롤백)
  - 파티셔닝은 독립적인 Worker Step을 구성하고 각각의 Worker Step은  ItemReader, ItemProcessor, ItemWriter 등을 가지고 있는 완전체.
  - 각각의 파티션은 독립적인 트랜잭션을 가짐. (같은 Step을 실행하지만 독립적인 ExecutionContext를 갖는다).

### 주의
샘플 코드이기 때문에 new SimpleAsyncTaskExecutor() 를 사용했지만 실제 운영하는 서비스에서는 TaskExecutor를 구현하여 사용하는 것이 좋음.
new SimpleAsyncTaskExecutor()는 쓰레드를 제한없이 계속 생성할 수 있기 때문.


