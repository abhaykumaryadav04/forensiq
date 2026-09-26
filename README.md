# ForensiQ

## AI-Based Fake Identity & Document Screening System

ForensiQ is an AI-assisted identity and document screening platform designed to detect potentially manipulated, forged, or suspicious identity documents.

The system combines document image processing, OCR, information extraction, verification mechanisms, image forensics, AI-based analysis, and centralized risk assessment to produce an explainable screening verdict.

## Problem Statement

Identity documents such as passports, Aadhaar cards, PAN cards, driving licences, and other official documents can be digitally manipulated or forged.

Traditional manual verification can be time-consuming and may not consistently detect sophisticated image-level alterations.

ForensiQ provides an automated screening pipeline that analyzes uploaded or camera-captured documents and identifies suspicious characteristics.

## Main Objective

The objective of ForensiQ is to provide a single automated platform capable of:

- Capturing identity documents through a camera
- Validating uploaded document files
- Extracting text using OCR
- Identifying document types
- Extracting important identity fields
- Validating MRZ information
- Reading QR codes and barcodes
- Detecting suspicious image manipulation
- Performing AI-based document analysis
- Comparing information across different verification sources
- Calculating a centralized risk score
- Generating an explainable final verdict
- Maintaining document integrity and audit information

## Core Workflow

```text
Camera / File Upload
        |
        v
Document Capture
        |
        v
File Validation
        |
        v
SHA-256 Hash
        |
        v
Secure Storage
        |
        v
PDF / Image Processing
        |
        v
OpenCV Preprocessing
        |
        v
OCR
        |
        v
Document Classification
        |
        v
Information Extraction
        |
        +-------------------+
        |                   |
        v                   v
   MRZ Validation      QR / Barcode
        |                   |
        +---------+---------+
                  |
                  v
          Image Forensics
                  |
                  v
             AI Analysis
                  |
                  v
        Cross-Field Verification
                  |
                  v
          Central Risk Engine
                  |
                  v
             Final Verdict
                  |
                  v
          Automatic Report
