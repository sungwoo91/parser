# Description
- Producer-Consumer 패턴을 응용하여 파일 라인을 특정 조건에 맞게 여러 파일로 분리한다.

# Implementation
- Producer 에서는 ParallelStream을 이용하여 멀티쓰레딩을 하였고, Consumer 에서는 쓰레드 풀을 이용하여 멀티쓰레딩을 구현 하였다.

# 실행 순서
1. git clone https://github.com/sungwoo91/parser.git 으로 레포지토리를 다운받는다.
2. maven dependency 를 받아온다.
3. 실행 시 argument 로 읽어올 파일 위치, 저장할 디렉토리 위치, 파티션 수 를 입력한다.
 ex) /Users/leesungwoo/IdeaProjects/parser/src/main/resources/words.txt /Users/leesungwoo/IdeaProjects/parser/src/main/resources/output 20
4. Starter.main을 실행시킨다.

# 아키텍처

![Image of Architecture](https://github.com/sungwoo91/parser/blob/master/src/main/resources/architecture/architecture.001.jpeg?raw=true)

# 클래스 설계

![Image of ClassDiagram](https://github.com/sungwoo91/parser/blob/master/src/main/resources/classdiagram/classdiagram.001.jpeg?raw=true)

# 클래스
> Starter
- main 함수를 가지고 있는 시작 클래스
- 전체 파티션을 큐 형태로 관리
- 인자값을 받아 Producer 와 Consumer 쓰레드 들을 시작하고 관리 

> Producer
- Runnable 인터페이스를 상속한 인터페이스
- produce 메소드를 인터페이스로 가짐

> FileProducer
- Producer 를 구현한 클래스
- run 메소드와 produce 메소드를 구현
- 입력된 파일의 내용을 스트림 형태로 읽어온 다음 정규표현식으로 필터

> Consumer
- Runnable 인터페이스를 상속한 인터페이스
- consume 메소드를 인터페이스로 가짐

> FileConsumer
- Consumer 를 구현한 클래스
- run 메소드와 consume 메소드를 구현
- 파티션(큐) 에서 단어들을 가져와 중복 되지 않은 단어만 특정 파일에 출력
