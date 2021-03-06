// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#include "cf-handler.h"
#include <dirent.h>
#include <stdio.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <vespa/defaults.h>
#include <vespa/config/common/configsystem.h>
#include <vespa/config/common/exceptions.h>

#include <vespa/log/log.h>
LOG_SETUP(".cf-handler");

CfHandler::CfHandler() : _subscriber() {}

CfHandler::~CfHandler()
{
}

void
CfHandler::subscribe(const std::string & configId, uint64_t timeoutMS)
{
    _handle = _subscriber.subscribe<LogforwarderConfig>(configId, timeoutMS);
}

namespace {
std::string
cfFilePath() {
    std::string path = vespa::Defaults::underVespaHome("var/db/vespa/splunk");
    DIR *dp = opendir(path.c_str());
    if (dp == NULL) {
        if (errno != ENOENT || mkdir(path.c_str(), 0755) != 0) {
            perror(path.c_str());
        }
    } else {
        closedir(dp);
    }
    path += "/deploymentclient.conf";
    return path;
}
}

void
CfHandler::doConfigure()
{
    std::unique_ptr<LogforwarderConfig> cfg(_handle->getConfig());
    const LogforwarderConfig& config(*cfg);

    std::string path = cfFilePath();
    std::string tmpPath = path + ".new";
    FILE *fp = fopen(tmpPath.c_str(), "w");
    if (fp == NULL) return;

    fprintf(fp, "[deployment-client]\n");
    fprintf(fp, "clientName = %s\n", config.clientName.c_str());
    fprintf(fp, "\n");
    fprintf(fp, "[target-broker:deploymentServer]\n");
    fprintf(fp, "targetUri = %s\n", config.deploymentServer.c_str());

    fclose(fp);
    rename(tmpPath.c_str(), path.c_str());
}

void
CfHandler::check()
{
    if (_subscriber.nextConfig(0)) {
        doConfigure();
    }
}

constexpr uint64_t CONFIG_TIMEOUT_MS = 30 * 1000;

void
CfHandler::start(const char *configId)
{
    LOG(debug, "Reading configuration with id '%s'", configId);
    try {
        subscribe(configId, CONFIG_TIMEOUT_MS);
    } catch (config::ConfigTimeoutException & ex) {
        LOG(warning, "Timout getting config, please check your setup. Will exit and restart: %s", ex.getMessage().c_str());
        exit(EXIT_FAILURE);
    } catch (config::InvalidConfigException& ex) {
        LOG(error, "Fatal: Invalid configuration, please check your setup: %s", ex.getMessage().c_str());
        exit(EXIT_FAILURE);
    } catch (config::ConfigRuntimeException& ex) {
        LOG(error, "Fatal: Could not get config, please check your setup: %s", ex.getMessage().c_str());
        exit(EXIT_FAILURE);
    }
}
