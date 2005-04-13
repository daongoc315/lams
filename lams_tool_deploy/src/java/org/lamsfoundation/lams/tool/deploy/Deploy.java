/*
 *Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)
 *
 *This program is free software; you can redistribute it and/or modify
 *it under the terms of the GNU General Public License as published by
 *the Free Software Foundation; either version 2 of the License, or
 *(at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 *USA
 *
 *http://www.gnu.org/licenses/gpl.txt
 */

package org.lamsfoundation.lams.tool.deploy;

/**
 * Tool Deployer Main Class
 * @author chris
 */
public class Deploy
{
    
    /** Creates a new instance of Deploy */
    public Deploy()
    {
    }
    
    /**
     * Runs the Deploy
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception
    {
        
        if ((args.length != 1) || (args[0] == null))
        {
            throw new IllegalArgumentException("Usage: Deployer <properties_file_path>");
        }
        
        System.out.println("Starting Tool Deploy");
        try
        {
            System.out.println("Reading Configuration File");
            DeployConfig config =  new DeployConfig(args[0]);
            
            System.out.println("Running Tool DB Deploy");
            ToolDBDeployTask dbDeployTask = new ToolDBDeployTask();
            dbDeployTask.setDbUsername(config.getDbUsername());
            dbDeployTask.setDbPassword(config.getDbPassword());
            dbDeployTask.setDbDriverClass(config.getDbDriverClass());
            dbDeployTask.setDbDriverUrl(config.getDbDriverUrl());
            dbDeployTask.setToolInsertScriptPath(config.getToolInsertScriptPath());
            dbDeployTask.setToolLibraryInsertScriptPath(config.getToolLibraryInsertScriptPath());
            dbDeployTask.setToolActivityInsertScriptPath(config.getToolActivityInsertScriptPath());
            dbDeployTask.setToolTablesScriptPath(config.getToolTablesScriptPath());
            dbDeployTask.execute();
            
            System.out.println("Updating application.xml");
            AddWebAppToApplicationXmlTask addWebAppTask =  new AddWebAppToApplicationXmlTask();
            addWebAppTask.setLamsEarPath(config.getLamsEarPath());
            addWebAppTask.setContextRoot(config.getToolContextRoot());
            addWebAppTask.setWebUri(config.getToolWebUri());
            addWebAppTask.execute();
            
            System.out.println("Deploying files to ear");
            DeployFilesTask deployFilesTask = new DeployFilesTask();
            deployFilesTask.setLamsEarPath(config.getLamsEarPath());
            deployFilesTask.setDeployFiles(config.getDeployFiles());
            deployFilesTask.execute();
            
            System.out.println("Activating Tool in LAMS");
            ToolDBActivateTask dbActivateTask = new ToolDBActivateTask();
            dbActivateTask.setDbUsername(config.getDbUsername());
            dbActivateTask.setDbPassword(config.getDbPassword());
            dbActivateTask.setDbDriverClass(config.getDbDriverClass());
            dbActivateTask.setDbDriverUrl(config.getDbDriverUrl());
            dbActivateTask.setLearningLibraryId(dbDeployTask.getLearningLibraryId());
            dbActivateTask.setToolId(dbDeployTask.getToolId());
            dbActivateTask.execute();
            
            System.out.println("Tool Deployed");
        }
        catch (Exception ex)
        {
            System.out.println("TOOL DEPLOY FAILED");
            ex.printStackTrace();
        }
    }
    
}
