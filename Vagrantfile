# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do | config|
  config.vm.box = "ubuntu/focal64"

  config.vm.define "respond_process_repository" do |server|
      server.vm.network "private_network", ip: "192.168.87.10"
      server.vm.provision "ansible" do |ansible|
        ansible.playbook = "process_repository/deploy.yml"
        ansible.limit = "all,localhost" # Prevent ansible from ignoring localhost tasks
      end
  end
  config.vm.define "respond_selfhealing" do |server|
      server.vm.network "private_network", ip: "192.168.87.11"
      server.vm.provision "ansible" do |ansible|
        ansible.playbook = "selfhealing/deploy.yml"
        ansible.limit = "all,localhost" # Prevent ansible from ignoring localhost tasks
      end
  end
end
