package com.tickatch.paymentservice;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.library.Architectures;
import java.util.Arrays;

@AnalyzeClasses(
    packages = "com.tickatch.paymentservice",
    importOptions = ImportOption.DoNotIncludeTests.class)
class LayeredArchitectureTest {

  private static final String[] DOMAIN_PACKAGES = {
    "com.tickatch.paymentservice.example.domain..",
  };

  @ArchTest
  public void layeredArchitecture(JavaClasses classes) {
    Architectures.layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .layer("Presentation")
        .definedBy("com.tickatch.paymentservice..presentation..")
        .layer("Application")
        .definedBy("com.tickatch.paymentservice..application..")
        .layer("Domain")
        .definedBy("com.tickatch.paymentservice..domain..")
        .layer("Infrastructure")
        .definedBy("com.tickatch.paymentservice..infrastructure..")
        // Presentation은 하위 계층(Application, Domain, Infrastructure)만 접근 가능
        .whereLayer("Presentation")
        .mayOnlyAccessLayers("Application", "Domain", "Infrastructure")
        // Application은 Domain, Infrastructure만 접근 가능
        .whereLayer("Application")
        .mayOnlyAccessLayers("Domain", "Infrastructure")
        // Domain은 Infrastructure만 접근 가능
        .whereLayer("Domain")
        .mayOnlyAccessLayers("Infrastructure")
        // Infrastructure는 어떤 레이어에서도 접근할 수 없음
        .whereLayer("Infrastructure")
        .mayNotBeAccessedByAnyLayer()
        // global 패키지는 모든 계층에서 자유롭게 접근 가능하도록 제외
        .ignoreDependency(
            DescribedPredicate.alwaysTrue(),
            JavaClass.Predicates.resideInAPackage("com.tickatch.paymentservice.global.."))
        // 빈 레이어 허용
        .allowEmptyShould(true)
        .check(classes);
  }

  @ArchTest
  public void domainsShouldNotDependOnEachOther(JavaClasses classes) {
    // 도메인이 1개 이하면 검증 스킵
    if (DOMAIN_PACKAGES.length <= 1) {
      return;
    }

    for (String domainPackage : DOMAIN_PACKAGES) {
      // 해당 도메인 패키지에 클래스가 없으면 스킵
      if (!hasClassesInPackage(classes, domainPackage)) {
        continue;
      }

      String[] otherDomains =
          Arrays.stream(DOMAIN_PACKAGES)
              .filter(pkg -> !pkg.equals(domainPackage))
              .filter(pkg -> hasClassesInPackage(classes, pkg))
              .toArray(String[]::new);

      if (otherDomains.length == 0) {
        continue;
      }

      noClasses()
          .that()
          .resideInAPackage(domainPackage)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(otherDomains)
          .because("도메인 간에는 서로 의존할 수 없습니다 (global 제외)")
          .check(classes);
    }
  }

  @ArchTest
  public void globalShouldNotDependOnDomains(JavaClasses classes) {
    // 존재하는 도메인만 필터링
    String[] existingDomains =
        Arrays.stream(DOMAIN_PACKAGES)
            .filter(pkg -> hasClassesInPackage(classes, pkg))
            .toArray(String[]::new);

    // 도메인이 없으면 스킵
    if (existingDomains.length == 0) {
      return;
    }

    // global 패키지가 없으면 스킵
    if (!hasClassesInPackage(classes, "com.tickatch.paymentservice.global..")) {
      return;
    }

    noClasses()
        .that()
        .resideInAPackage("com.tickatch.paymentservice.global..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(DOMAIN_PACKAGES)
        .because("global 패키지는 어떤 도메인에도 의존해서는 안됩니다")
        .check(classes);
  }

  /** 특정 패키지에 클래스가 존재하는지 확인. */
  private boolean hasClassesInPackage(JavaClasses classes, String packagePattern) {
    String regex = packagePattern.replace("..", ".*").replace(".", "\\.");

    return classes.stream().anyMatch(clazz -> clazz.getPackageName().matches(regex));
  }
}
