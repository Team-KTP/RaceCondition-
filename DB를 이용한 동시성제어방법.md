## 1. **비관적 락 (Pessimistic Locking)**
동시성 충돌이 자주 발생할 것이라고 가정하고, 데이터에 접근할 때마다 미리 락을 걸어 다른 트랜잭션의 접근을 차단하는 방식입니다.

### 핵심 개념
***비관적 락*** 은 동시성 문제를 막기 위해 미리 락을 설정하는 전략 전체를 의미하며, 그 내부에 배타적 락과 공유 락이 포함됩니다.

_**배타적 락(Exclusive Lock)**_ 은 쓰기하는 동안 데이터를 독점하는 락 자체를 뜻하며, **PESSIMISTIC_WRITE 모드**가 이에 해당합니다.

### 동작 방식
- 데이터에 배타적 락을 걸어 다른 트랜잭션의 읽기와 쓰기를 모두 차단합니다
- SQL 로는 `SELECT ... FOR UPDATE `쿼리가 실행됩니다
- 락을 획득한 트랜잭션만 해당 데이터에 접근 가능합니다
- 트랜잭션이 커밋되거나 롤백될 때까지 락이 유지됩니다

### 예시 코드
~~~ java
    @Lock(LockModeType.PESSIMISTIC_WRITE) // JPA에서 비관적 락 설정
    @Query("SELECT s FROM Stock s WHERE s.id = :id")
    Optional<Stock> findByIdWithPessimisticLock(Long id);
~~~

### 실제 실행 쿼리
~~~ sql
SELECT s1_0.id,s1_0.product_id,s1_0.quantity
FROM stock s1_0
WHERE s1_0.id=?
FOR UPDATE -- - FOR UPDATE가 있으면 조회한 행에 **배타적 락(Exclusive Lock)** 을 겁니다.
~~~