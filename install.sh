#!/bin/sh

set -e

installPrism() {
	rm -rf prism
	git clone https://github.com/prismmodelchecker/prism prism
	cd prism/prism
	git checkout v4.5
	make -j$(nproc --all)
	make install
	echo "[INFO]: Finished installing PRISM"
}

installStamina() {
	dir=$(pwd)
	cd stamina
	make -j$(nproc --all) PRISM_HOME=$dir/prism/prism
	echo "[INFO]: Finished installing STAMINA"
}

setEnvironmentVariable() {
	touch ~/.staminarc
	echo "export_JAVA_OPTIONS=-Xmx12288m" >> ~/.staminarc
	echo "export PRISM_HOME=$(pwd)/prism/prism" >> ~/.staminarc
	echo "export STAMINA_HOME=$(pwd)/stamina" >> ~/.staminarc
}

installPrism
installStamina
setEnvironmentVariable
