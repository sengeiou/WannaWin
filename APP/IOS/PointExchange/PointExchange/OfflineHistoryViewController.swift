//
//  OfflineHistoryViewController.swift
//  PointExchange
//
//  Created by yiner on 2018/8/12.
//  Copyright © 2018年 WannaWin. All rights reserved.
//

import UIKit
import RxCocoa
import RxSwift
import RxDataSources

class OfflineHistoryViewController: UIViewController {

	var orders = [Order]()
	var disposeBag = DisposeBag()
	@IBOutlet weak var tableView: UITableView!

	override func viewDidLoad() {
        super.viewDidLoad()
		self.tableView.rowHeight = 95
		self.tableView.register(UINib(nibName: "HistoryTableViewCell", bundle: nil), forCellReuseIdentifier: "orderCell")
		ServerConnector.getOrders(intervalTime: "101010101"){ (result, orders) in
			if result {
				self.orders = orders
                if orders.count == 0{
                    self.tableView.isHidden = true
                }else{
                    self.tableView.isHidden = false
                    self.setDataSource()
                }
				
			}
			
		}
    }
	private func getDatas()->Observable<[SectionModel<String,Order>]>{
		let items = (0 ..< self.orders.count).map {i in
			self.orders[i]
		}
		let observable = Observable.just([SectionModel(model: "order", items: items)])
		return observable
	}
	private func setDataSource(){
		let observable = Observable.empty().asObservable()
			.startWith(())
			.flatMapLatest(getDatas)
			.share(replay: 1)
		let dataSource = RxTableViewSectionedReloadDataSource<SectionModel<String,Order>>(configureCell: { (dataSource,view,indexPath,element) in
			var cell = self.tableView.dequeueReusableCell(withIdentifier: "orderCell") as? HistoryTableViewCell
			if cell == nil {
				cell = UITableViewCell(style: .default, reuseIdentifier: "orderCell") as? HistoryTableViewCell
			}
			let imageURL = URL(string: (self.orders[indexPath.row].merchantLogoURL?.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed))!)
			cell?.logoImage.kf.indicatorType = .activity
			cell?.logoImage.kf.setImage(with: imageURL)
			cell?.pointLabel.text = String(stringInterpolationSegment: element.pointsNeeded!)
			cell?.dateTitleLabel.text = "消费日期"
			cell?.dateLabel.text = element.time
			return cell!
		})
		observable.bind(to:self.tableView.rx.items(dataSource: dataSource)).disposed(by: disposeBag)
		
		self.tableView.rx.modelSelected(Order.self)
			.subscribe(onNext:{ value in
				let sb = UIStoryboard(name: "User", bundle: nil)
				let vc = sb.instantiateViewController(withIdentifier: "HistoryOfflineDetailViewController") as! HistoryOfflineDetailViewController
				vc.order = value
				self.navigationController?.pushViewController(vc, animated: true)
			}).disposed(by: disposeBag)
	}

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
