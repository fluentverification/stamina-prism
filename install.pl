#!/usr/bin/perl

##########################################
# DEPRICATED INSTALL SCRIPT
#
# Rather than using this script, use the install.sh script
# included in this directory
##########################################

use Cwd qw(cwd);

# First we need to clone prism
my $originalCwd = cwd;
installPrism($originalCwd);
installStamina($originalCwd);
setEnvVariable();
my PRISM_TAG = "v4.8";

sub installPrism($originalCwd) {
    my @command = (
        "cd $originalCwd && rm -rf prism && git clone https://github.com/prismmodelchecker/prism prism && cd prism/prism && git checkout v$PRISM_TAG && make -j4",
    );
    system(shift @command);
}

sub installStamina() {
    my $dir = cwd;
    my @command = (
        "cd $originalCwd && rm -rf stamina-prism && git clone https://github.com/fluentverification/stamina-prism.git && cd stamina-prism/stamina && make -j4 PRISM_HOME=$dir/prism/prism"
    );
    system(shift @command);
}

sub setEnvVariable() {
    my @command = (
        "export _JAVA_OPTIONS=-Xmx12288m"
    );
    system(shift @command);
}
