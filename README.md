# Mini-C
 Mini-C 스캐너 2020년 1학기 형식언어, 손윤식 교수님
 
 ## 사용법
 java -jar `jar 파일명` `mini-c 파일명`
 
 ## 구조
 - Token.java
   - 스캐너의 리턴 타입인 토큰을 정의한 클래스
   - TokenType, TokenValue를 통합 저장함
   - 원본 파일에서 확장 토큰에 관한 정의를 제외하고는 동일
 
 - TokenType.java
   - Token의 종류를 판별하는 데에 이용할 수 있는 enum 값이 존재하는 java 파일
   - 일부 확장 토큰이 선언됨
  
 - EScanner.java
   - 토큰을 스캔하는 스캐너
   - 확장된 토큰 인식기능이 존재함
   - JAVA Iterator<T> 인터페이스를 구현함
 - UnicodeTable.java
   - 현재 문자(char)이 유니코드 범위에 존재하는지 확인해주는 클래스
   - mini-c의 구현상 필요한 범위인 영문자 대문자, 영문자 소문자, ASCII 숫자 범위가 미리 구현됨
   - 정규식의 범위지정과 유사한 기능을 수행하기 위해서 만듦(아마 정규식을 이용하는 것은 해당 과제의 목표가 아닐 것이라 예상되므로)
