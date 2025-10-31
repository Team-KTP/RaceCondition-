~~~ java
   @DisplayName("동시에 재고 감소 요청이 100가 들어온다.")
    @Test
    void decrease2() throws InterruptedException {
        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertThat(stock.getQuantity()).isZero();
    }
~~~

## 현재 상황

- 재고가 100개인 상품에 대한 재고 차감이 동시에 100개의 요청이 들어왔을 경우 모두 차감 후 남은 수량이 0개라고 예상한다.
- 막상 결과를 보니 0개가 되지 않는다.

## 이유는 무엇일까?

- `Race Condition` 이 발생하였기 때문이다.
- **Race Condition** 이란?
    - **_두 개 이상의 프로세스나 스레드가 공유 자원에 동시에 접근_** 할 때, 실행 순서에 따라 결과가 달라지는 문제 상황
    - 여러 작업이 동일한 자원을 동시에 읽거나 쓰려고 경쟁하면서 발생
#### 현재 상황에 적용하자면
- 동시성 문제로 인해 여러 스레드가 동일한 재고 수량을 읽고 동시에 차감하는 상황이 발생했기 때문이다.
- 쉽게 설명하자면 ThreadA가 데이터를 읽고 갱신 하기 전에 ThreadB가 동일한 데이터를 읽고 갱신하는 상황이 발생한 것이다.
    1. ThreadA가 재고 수량 100을 읽음 
    2. ThreadA가 수량을 갱신하기 전에 ThreadB가 재고 수량 100을 읽음
    3. ThreadA가 재고 수량을 99로 갱신
    4. ThreadB가 재고 수량을 99로 갱신
    - 결과적으로 재고 수량이 98이 아닌 99가 되는 상황이 발생

## 어떻게 해결해야 할 수 있을까?
쉽게 말해, 한 스레드가 자원에 접근하여 작업을 완료한 후 다른 스레드가 접근할 수 있도록 하게 하면 된다. 
동시성 문제를 해결하기 위해 여러 가지 방법이 있다.
#### **낙관적 락 (Optimistic Locking)**
- 데이터 충돌이 드물다고 가정하고 락을 걸지 않고, 충돌이 발생했을 때만 재시도하는 방식
  - **동작 방식**
    - 데이터에 버전 번호(version) 또는 타임스탬프를 추가합니다
    - 데이터를 읽을 때 버전 정보를 함께 가져옵니다
    - 수정 시 읽었던 버전과 현재 버전을 비교합니다
    - 버전이 다르면 충돌로 판단하고 롤백 후 재시도합니다
  - **장점**
    - 락을 사용하지 않아 성능이 우수합니다
    - 읽기 작업이 많은 환경에서 효과적입니다
    - 데드락 발생 가능성이 없습니다
  - **단점**
    - 충돌이 자주 발생하면 재시도 비용이 커집니다
    - 개발자가 재시도 로직을 직접 구현해야 합니다    

#### **비관적 락 (Pessimistic Locking)**
- 데이터에 접근할 때마다 락을 걸어 다른 트랜잭션이 접근하지 못하도록 하는 방식
  - **장점**
    - 충돌을 원천적으로 방지합니다.
    - 데이터 정합성을 강력하게 보장합니다
    - 충돌이 빈번한 환경에서 재시도 비용이 없습니다
  - **단점**
    - 락 대기로 인한 성능 저하가 발생합니다
    - 데드락이 발생할 수 있습니다
    - 동시 처리량이 감소합니다
#### **분산 락 (Distributed Locking)**
- 여러 인스턴스에서 동시에 접근하는 경우 분산 락을 사용하여 동시성 문제를 해결하는 방법

각 방법마다 장단점이 있으므로, 상황에 맞게 적절한 방법을 선택하는 것이 중요하다.

## 단일 서버 환경에서 Lock이 작동하는 이유
- 단일 서버에서는 하나의 JVM(Java Virtual Machine)만 실행됩니다
- 모든 스레드가 같은 JVM의 메모리를 공유하므로, `synchronized` 나 `Lock` 을 사용하면 이들 스레드 간 동시성 제어가 가능합니다.

## 분산 환경에서 분산 락을 사용하는 이유
- 각 서버의 JVM은 독립적인 메모리를 가지고 있기에 JVM 간 메모리 공유 불가, 공유 저장소(Redis, MySQL 등) 를 통해 여러 서버 간 동시성을 제어해야 한다.
- 분산락 사용