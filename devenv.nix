{ pkgs, lib, config, inputs, ... }:

{
  languages.java = {
    enable = true;
    jdk.package = pkgs.jdk21;
    maven.enable = true;
  };

  processes.app = {
    ports.http.allocate = 8080;
    exec = ''
      exec mvn -q spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=${toString config.processes.app.ports.http.value}"
    '';
  };

  enterShell = ''

    echo -e "\n\n\n\n DEVENV CARREGADO \n\n\n\n"

     '';
}
