//
//  SearchResultMerchantViewController.swift
//  PointExchange
//
//  Created by yiner on 2018/8/23.
//  Copyright © 2018年 WannaWin. All rights reserved.
//

import UIKit

class SearchResultMerchantViewController: UIViewController,UITableViewDelegate,UITableViewDataSource {

	@IBOutlet var tableView: UITableView!
    var noResultLabel:UILabel?
	var searchResults = [Merchant]()
	var keyword = ""
	var start = 0
	var end = 9
	
	override func  viewDidAppear(_ animated: Bool) {
		self.tableView.frame = self.view.bounds
	}
	
	override func viewDidLoad() {
        super.viewDidLoad()
        noResultLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 150, height: 10))
        noResultLabel?.center.x = self.view.center.x
        noResultLabel?.center.y = self.view.center.y-80
        noResultLabel?.text = "暂无您搜索的结果"
        self.view.addSubview(self.noResultLabel!)
        noResultLabel?.isHidden = true
        
		self.tableView.delegate = self
		self.tableView.dataSource = self
		self.tableView.register(UINib(nibName: "SearchTableViewCell", bundle: nil), forCellReuseIdentifier: "searchCell")
		
		self.tableView.es.addPullToRefresh { [weak self] in
			self?.search(keyword: (self?.keyword)!)
			self?.tableView.es.stopPullToRefresh()
			self?.start = 0
			self?.end = 6
		}
		
		self.tableView.es.addInfiniteScrolling {
			self.start = self.start + 6
			self.end = self.end + 6
			self.loadMore()
			
		}
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
	
	func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
		var cell = tableView.dequeueReusableCell(withIdentifier: "searchCell") as? SearchTableViewCell
		if cell == nil {
			cell = UITableViewCell(style:.default, reuseIdentifier:"searchCell") as? SearchTableViewCell
		}
		let imageURL = URL(string: (searchResults[indexPath.row].logoURL?.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed))!)
		cell?.logoImageView.kf.indicatorType = .activity
		cell?.logoImageView.kf.setImage(with: imageURL)
		cell?.title.text = searchResults[indexPath.row].name
		cell?.descriptionLabel.text = searchResults[indexPath.row].description
        
        cell?.selectionStyle = .none
		return cell!
	}
	
	func search(keyword:String){
		self.keyword = keyword
		ServerConnector.searchMerchant(start: self.start, end: self.end, keyword: keyword){(result, merchants) in
			if result {
                if merchants?.count == 0{
                    self.tableView.isHidden = true
                    self.noResultLabel?.isHidden = false
                }else{
                    self.tableView.isHidden = false
                    self.noResultLabel?.isHidden = true
                    self.searchResults = merchants!
                    self.tableView.reloadData()
                }
				
				
			}
		}
	}
	func loadMore(){
		ServerConnector.searchMerchant(start: self.start, end: self.end, keyword: keyword){(result, merchants) in
			if result {
				self.searchResults += merchants!
				
				if (merchants?.count)! < 6{
					self.tableView.es.noticeNoMoreData()
				}else{
					self.tableView.es.stopLoadingMore()
				}
				
				self.tableView.reloadData()
				
			}
		}
	}
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let storyBoard = UIStoryboard(name:"Discover", bundle:nil)
        let view = storyBoard.instantiateViewController(withIdentifier: "MerchantDetailViewController") as! MerchantDetailViewController
        view.merchant = searchResults[indexPath.row]
        self.presentingViewController?.navigationController?.pushViewController(view, animated: true)
    }
	
	func numberOfSections(in tableView: UITableView) -> Int {
		return 1
	}
	func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
		return searchResults.count
	}

}
