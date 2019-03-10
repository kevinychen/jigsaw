//
//  ViewController.swift
//  jigsaw
//
//  Created by Kevin Chen on 3/9/19.
//  Copyright © 2019 Kevin Chen. All rights reserved.
//

import AVFoundation
import UIKit

enum Direction {
    case up
    case left
    case right
    case down
}

class ViewController: UIViewController {

    var iCapturePhotoOutput: AVCapturePhotoOutput?
    var iVideoPreviewLayer: AVCaptureVideoPreviewLayer?
    var images: [Direction: Data] = [:]
    var currentDirection: Direction?

    @IBOutlet weak var serverHost: UITextField!
    @IBOutlet weak var cameraView: UIImageView!
    @IBOutlet weak var upEdgeButton: UIButton!
    @IBOutlet weak var leftEdgeButton: UIButton!
    @IBOutlet weak var rightEdgeButton: UIButton!
    @IBOutlet weak var downEdgeButton: UIButton!
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!

    override func viewDidLoad() {
        super.viewDidLoad()

        serverHost.delegate = self

        guard let captureDevice = AVCaptureDevice.default(for: AVMediaType.video) else {
            fatalError("No video capture device")
        }

        let captureSession = AVCaptureSession()
        let capturePhotoOutput = AVCapturePhotoOutput()
        capturePhotoOutput.isHighResolutionCaptureEnabled = true
        do {
            let input = try AVCaptureDeviceInput(device: captureDevice)
            captureSession.addInput(input);
        } catch {
            print(error)
        }

        captureSession.addOutput(capturePhotoOutput)
        iCapturePhotoOutput = capturePhotoOutput

        let videoPreviewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
        videoPreviewLayer.videoGravity = AVLayerVideoGravity.resizeAspectFill
        videoPreviewLayer.frame = cameraView.bounds
        cameraView.layer.addSublayer(videoPreviewLayer)
        iVideoPreviewLayer = videoPreviewLayer

        captureSession.startRunning()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }

    @IBAction func sendUpEdge(_ sender: Any) {
        currentDirection = Direction.up
        sendPhoto()
        upEdgeButton.setTitle("✔", for: .normal)
    }

    @IBAction func sendLeftEdge(_ sender: Any) {
        currentDirection = Direction.left
        sendPhoto()
        leftEdgeButton.setTitle("✔", for: .normal)
    }

    @IBAction func sendRightEdge(_ sender: Any) {
        currentDirection = Direction.right
        sendPhoto()
        rightEdgeButton.setTitle("✔", for: .normal)
    }

    @IBAction func sendDownEdge(_ sender: Any) {
        currentDirection = Direction.down
        sendPhoto()
        downEdgeButton.setTitle("✔", for: .normal)
    }

    private func sendPhoto() {
        let photoSettings = AVCapturePhotoSettings()
        photoSettings.isAutoStillImageStabilizationEnabled = true
        photoSettings.isHighResolutionPhotoEnabled = true
        photoSettings.flashMode = .off
        iCapturePhotoOutput?.capturePhoto(with: photoSettings, delegate: self)
    }

    @IBAction func solve(_ sender: Any) {
        let host = serverHost.text ?? "" == "" ? "[default]" : serverHost.text!
        guard let url: URL = URL(string: "http://\(host)/solve") else {
            return print("invalid URL")
        }

        var request: URLRequest = URLRequest(url: url)
        request.httpMethod = "POST"

        let boundary = "Boundary-\(NSUUID().uuidString)"
        request.setValue("multipart/form-data; boundary=" + boundary, forHTTPHeaderField: "Content-Type")

        var formData = Data()
        for (direction, image) in images {
            append(line: "--\(boundary)", data: &formData)
            append(line: "Content-Disposition: form-data; name=\"\(direction)\"; filename=\"\(direction)Image\"", data: &formData)
            append(line: "Content-Type: image/jpg", data: &formData)
            append(line: "", data: &formData)
            formData.append(image)
            append(line: "", data: &formData)
        }
        append(line: "--\(boundary)--", data: &formData)

        request.setValue(String(formData.count), forHTTPHeaderField: "Content-Length")
        request.httpBody = formData
        request.httpShouldHandleCookies = false

        let session = URLSession(configuration: .default)
        activityIndicator.startAnimating()
        let task = session.dataTask(with: request) { (data, response, error) in
            DispatchQueue.main.async {
                self.activityIndicator.stopAnimating()
            }
        }
        task.resume()

        images.removeAll()
        upEdgeButton.setTitle("up", for: .normal)
        leftEdgeButton.setTitle("left", for: .normal)
        rightEdgeButton.setTitle("right", for: .normal)
        downEdgeButton.setTitle("down", for: .normal)
    }

    private func append(line: String, data: inout Data) {
        data.append("\(line)\r\n".data(using: String.Encoding.utf8, allowLossyConversion: false)!)
    }
}

extension ViewController: UITextFieldDelegate {
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        textField.resignFirstResponder()
        return true
    }
}

extension ViewController: AVCapturePhotoCaptureDelegate {

    func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
        guard let imageData = photo.fileDataRepresentation() else {
            return print("failed to get image data")
        }
        guard let capturedImage = UIImage.init(data: imageData, scale: 1.0) else {
            return print("failed to capture image")
        }
        guard let data = UIImageJPEGRepresentation(capturedImage, 1.0) else {
            return print("failed to fetch image data")
        }
        images[currentDirection!] = data
    }
}
