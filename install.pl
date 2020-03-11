#!/usr/bin/perl

use Cwd qw(cwd);

# First we need to clone prism
my $originalCwd = cwd;
installPrism($originalCwd);
installStamina($originalCwd);
setEnvVariable();

sub installPrism($originalCwd) {
    my @command = (
        "cd $originalCwd && rm -rf prism && git clone https://github.com/prismmodelchecker/prism prism && cd prism/prism && git checkout v4.5 && make -j4",
    );
    system(shift @command);
}

sub installStamina() {
    my $dir = cwd;
    my @command = (
        "cd $originalCwd && rm -rf stamina && git clone https://github.com/fluentverification/stamina.git && cd stamina/stamina && make -j4 PRISM_HOME=$dir/prism/prism"
    );
    system(shift @command);
}

sub setEnvVariable() {
    my @command = (
        "export _JAVA_OPTIONS=-Xmx12288m"
    );
    system(shift @command);
}
