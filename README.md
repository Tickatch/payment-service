# project-interface

## 개요
이 문서는 [project-interface](https://github.com/tickatch/project-interface) 프로젝트를 기반으로 새로운 서비스 프로젝트를 생성하는 방법을 안내합니다.  

`project-interface`는 Tickatch 서비스 개발 시 공통으로 사용되는 코드, 라이브러리, 설정 등을 포함한 **프로젝트 템플릿** 역할을 합니다.

---

## 템플릿 사용 방법
템플릿 사용 방법에는 크게 두 가지가 있습니다.

### Github Template 기능 사용
1. GitHub 페이지에서 Use this template 버튼을 클릭하여 해당 프로젝트를 복제한 새로운 레포지토리를 생성합니다.

2. 해당 프로젝트를 로컬에 클론합니다.
``` bash
git clone https://github.com/tickatch/{새로운-서비스-레포}.git
cd {새로운-서비스-레포}
```

3. 다음 명령어 또는 IDE를 통해 패키지명 변경
``` bash
// 본 예시에서는 이해를 돕기 위해 새로운 패키지명을 ticketservice 로 사용하였습니다.
// 실제 사용 시에는 새로운 프로젝트의 패키지명을 사용해주시기 바랍니다.
mv src/main/java/com/tickatch/projectinterface src/main/java/com/tickatch/ticketservice
find src/main/java/com/tickatch/ticketservice -type f -name "*.java" | xargs sed -i '' 's/package com.tickatch.paymentservice/package com.tickatch.ticketservice/g'
find src/main/java/com/tickatch/ticketservice -type f -name "*.java" | xargs sed -i '' 's/import com.tickatch.paymentservice/import com.tickatch.ticketservice/g'

mv src/test/java/com/tickatch/projectinterface src/test/java/com/tickatch/ticketservice
find src/test/java/com/tickatch/ticketservice -type f -name "*.java" | xargs sed -i '' 's/package com.tickatch.paymentservice/package com.tickatch.ticketservice/g'
find src/test/java/com/tickatch/ticketservice -type f -name "*.java" | xargs sed -i '' 's/import com.tickatch.paymentservice/import com.tickatch.ticketservice/g'
```

4. IDE로 어플리케이션 진입점, 각종 환경변수 등을 수정합니다. 필수 수정 항목은 다음과 같습니다.
- ProjectInterfaceApplication.java
- build.gradle 의 description
- settings.gradle 의 rootProject.name
- application.yml 의 spring.application.name
- gradle.properties
- LayeredArchitectureTest 코드 내 패키지명
- 그 외 project-interface 또는 projectinterface로 되어있는 항목

5. gradle의 spotbugsMain, spotbugsTest, spotlessCheck, test 를 실행하여 프로젝트 문제가 없는지 확인합니다.

### Git Clone 기능 활용

1. GitHub에서 새로운 서비스 레포지토리를 생성합니다. 
   - 예: `user-service`, `order-service` 등
     
2. 다음 명령어 또는 github 페이지의 DownloadZip을 활용해 project-interface를 로컬로 클론합니다.

``` bash
git clone https://github.com/tickatch/project-interface.git
cd project-interface
```

3. 불필요한 Git 히스토리 제거 후 새로운 프로젝트 폴더로 이동

``` bash
rm -rf .git
cp -R . <새로운-서비스-레포>
cd <새로운-서비스-레포>
```

4. 다음 명령어 또는 IDE를 통해 패키지명 변경
``` bash
// 본 예시에서는 이해를 돕기 위해 새로운 패키지명을 ticketservice 로 사용하였습니다.
// 실제 사용 시에는 새로운 프로젝트의 패키지명을 사용해주시기 바랍니다.
mv src/main/java/com/tickatch/projectinterface src/main/java/com/tickatch/ticketservice
find src/main/java/com/tickatch/ticketservice -type f -name "*.java" | xargs sed -i '' 's/package com.tickatch.paymentservice/package com.tickatch.ticketservice/g'
find src/main/java/com/tickatch/ticketservice -type f -name "*.java" | xargs sed -i '' 's/import com.tickatch.paymentservice/import com.tickatch.ticketservice/g'

mv src/test/java/com/tickatch/projectinterface src/test/java/com/tickatch/ticketservice
find src/test/java/com/tickatch/ticketservice -type f -name "*.java" | xargs sed -i '' 's/package com.tickatch.paymentservice/package com.tickatch.ticketservice/g'
find src/test/java/com/tickatch/ticketservice -type f -name "*.java" | xargs sed -i '' 's/import com.tickatch.paymentservice/import com.tickatch.ticketservice/g'
```

5. IDE로 어플리케이션 진입점, 각종 환경변수 등을 수정합니다. 필수 수정 항목은 다음과 같습니다.
- ProjectInterfaceApplication.java
- build.gradle.kts 의 description
- settings.gradle.kts 의 rootProject.name
- application.yml 의 spring.application.name
- gradle.properties
- LayeredArchitectureTest 코드 내 패키지명
- 그 외 project-interface 또는 projectinterface로 되어있는 항목

6. gradle의 spotbugsMain, spotbugsTest, spotlessCheck, test 를 실행하여 프로젝트에 문제가 없는지 확인합니다.

7. 1번에서 만든 레포지토리와 프로젝트를 연결합니다.

``` bash
// Mac OS 의 경우 다음 파일이 생성되어 있을 수 있어 삭제합니다.
find . -name '.DS_Store' -type f -delete

git init
git add .
git commit -m "Initialize project from project-interface template"
git branch -M main
git remote add origin https://github.com/tickatch/<새로운-서비스-레포>.git
git push -u origin main
```

