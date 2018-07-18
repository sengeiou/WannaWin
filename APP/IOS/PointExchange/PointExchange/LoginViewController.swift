//
//  LoginViewController.swift
//  PointExchange
//
//  Created by Pan on 2018/7/5.
//  Copyright © 2018年 WannaWin. All rights reserved.
//

import UIKit
import Alamofire

class LoginViewController: UITableViewController{
    @IBOutlet weak var usernameField: UITextField!
    @IBOutlet weak var passwordField: UITextField!
    @IBOutlet weak var loginButton: UITableViewCell!
    
    
    var activityIndicator:UIActivityIndicatorView?
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        passwordField.delegate=self
        usernameField.delegate=self
        // Do any additional setup after loading the view.
    }
    

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    // 点击事件
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath:IndexPath){
        if (indexPath as NSIndexPath).section == 2 && (indexPath as NSIndexPath).row == 0{
            ServerConnector.login(phoneNum: usernameField.text!, password: passwordField.text!, callback: login)
            // 加载动画
            self.activityIndicator = ActivityIndicator.createWaitIndicator(parentView: self.tableView)
            self.activityIndicator?.startAnimating()
            
        }
    }
    
    // 登录完成后的操作
    func login(result:Bool){
        if result == true {
            self.saveUserInfo()
            
            self.activityIndicator?.stopAnimating()
            
            let alert = UIAlertController(title:"登录", message:"登录成功！", preferredStyle:.alert)
            let okAction=UIAlertAction(title:"确定", style:.default, handler:{ action in
                self.navigationController?.popViewController(animated: true)
            })
            alert.addAction(okAction)
            self.present(alert, animated: true, completion: nil)
        }
        else{
			let alert:UIAlertController!
			let reachable = NetworkReachabilityManager()
			if !(reachable?.isReachable)! {
				alert = UIAlertController(title:"登录", message:"网络连接失败，请刷新重试！", preferredStyle:.alert)
			}
			else {
				alert = UIAlertController(title:"登录", message:"登录失败！请检查用户名和密码是否正确", preferredStyle:.alert)
			}
            let okAction=UIAlertAction(title:"确定", style:.default, handler:{ action in
            })
            alert.addAction(okAction)
            self.present(alert, animated: true, completion: nil)
        }
        self.activityIndicator?.stopAnimating()
    }
    
    // 设置当前用户的用户名和密码，并存储到keychain
    func saveUserInfo(){
        User.saveToKeychain()
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = super.tableView(self.tableView, cellForRowAt: indexPath)
        cell.selectionStyle = .none
        return cell
        
    }
    
    // 每次输入完后判断输入是否合法
    @IBAction func isInputValid(_ sender: Any) {
        if usernameField.text != "" && passwordField.text != "" {
            loginButton.contentView.backgroundColor = UIColor.blue
            loginButton.isUserInteractionEnabled = true
        }
        else{
            loginButton.contentView.backgroundColor = UIColor.lightGray
            loginButton.isUserInteractionEnabled = false
        }
    }
    
    
}
